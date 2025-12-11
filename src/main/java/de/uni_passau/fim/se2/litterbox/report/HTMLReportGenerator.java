/*
 * Copyright (C) 2019-2024 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.report;

import de.uni_passau.fim.se2.litterbox.analytics.Issue;
import de.uni_passau.fim.se2.litterbox.analytics.MultiBlockIssue;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.FinderGroup;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.ComputationalThinkingScore;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.BlockCount;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.ScriptCount;
import de.uni_passau.fim.se2.litterbox.analytics.metrics.SpriteCount;
import java.util.*;
import java.util.stream.Collectors;

public class HTMLReportGenerator implements ReportGenerator {

    private final IssueTranslator translator;
    private final OutputStream outputStream;
    private final boolean closeStream;

    public HTMLReportGenerator(IssueTranslator translator, Path reportFile) throws IOException {
        this(translator, Files.newOutputStream(reportFile), true);
    }

    public HTMLReportGenerator(IssueTranslator translator, OutputStream outputStream) {
        this(translator, outputStream, false);
    }

    private HTMLReportGenerator(IssueTranslator translator, OutputStream outputStream, boolean closeStream) {
        this.translator = translator;
        this.outputStream = outputStream;
        this.closeStream = closeStream;
    }

    @Override
    public void generateReport(Program program, Collection<Issue> issues) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"").append(translator.getLocale().getLanguage()).append("\">\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>LitterBox Report</title>\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/scratchblocks@latest/build/scratchblocks.min.js\"></script>\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/scratchblocks@latest/build/translations-all.js\"></script>\n");
        html.append("<style>\n");
        html.append("body { font-family: sans-serif; margin: 2rem; background-color: #f4f4f4; }\n");
        html.append(".container { max-width: 900px; margin: 0 auto; background: #fff; padding: 2rem; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }\n");
        html.append("h1 { color: #333; text-align: center; }\n");
        html.append("h2 { color: #555; border-bottom: 2px solid #eee; padding-bottom: 0.5rem; margin-top: 2rem; }\n");
        html.append(".issue { border: 1px solid #ddd; border-radius: 4px; padding: 1rem; margin-bottom: 1rem; background: #fff; }\n");
        html.append(".issue-header { display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 0.5rem; }\n");
        html.append(".issue-title { font-size: 1.25em; font-weight: bold; color: #333; }\n");
        html.append(".issue-meta { font-size: 0.9em; color: #777; margin-bottom: 0.5rem; }\n");
        html.append(".hint { background: #e9ecef; padding: 1rem; border-radius: 4px; margin-bottom: 1rem; }\n");
        html.append("pre.blocks { background: #fff; border: none; padding: 0; overflow-x: auto; }\n");
        html.append("code.scratchblocks { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; font-weight: bold; padding: 2px 4px; border-radius: 4px; }\n");
        html.append(".severity { font-size: 0.8em; padding: 2px 6px; border-radius: 4px; color: #fff; }\n");
        html.append(".severity-ERROR { background-color: #dc3545; }\n");
        html.append(".severity-WARNING { background-color: #ffc107; color: #000; }\n");
        html.append(".severity-INFO { background-color: #17a2b8; }\n");
        html.append(".stats-container { display: flex; flex-wrap: wrap; justify-content: space-around; margin-bottom: 2rem; background: #fff; padding: 1rem; border-radius: 8px; box-shadow: 0 0 5px rgba(0,0,0,0.05); }\n");
        html.append(".stat-box { text-align: center; margin: 10px; min-width: 100px; }\n");
        html.append(".stat-value { font-size: 1.5em; font-weight: bold; color: #007bff; display: block; }\n");
        html.append(".stat-label { font-size: 0.9em; color: #666; }\n");
        html.append(".report-nav { margin-bottom: 2rem; }\n");
        html.append(".report-nav ul { list-style: none; padding: 1rem; margin: 0; display: flex; justify-content: center; gap: 15px; background: #f8f9fa; border-radius: 8px; border: 1px solid #e9ecef; }\n");
        html.append(".report-nav li { display: inline; }\n");
        html.append(".report-nav a { text-decoration: none; color: #495057; font-weight: bold; font-size: 1em; padding: 8px 16px; background-color: #fff; border: 1px solid #ced4da; border-radius: 5px; transition: all 0.2s ease; display: inline-block; }\n");
        html.append(".report-nav a:hover { text-decoration: none; background-color: #e2e6ea; border-color: #adb5bd; color: #212529; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }\n");
        html.append(".category-description { font-style: italic; color: #666; margin-bottom: 1.5rem; }\n");
        html.append(".back-to-top { text-align: right; margin-top: 10px; }\n");
        html.append(".back-to-top a { text-decoration: none; color: #999; font-size: 0.9em; }\n");
        html.append(".back-to-top a:hover { color: #007bff; }\n");
        
        // Filter CSS
        html.append(".filter-container { background: #f8f9fa; padding: 1rem; border-radius: 8px; margin-bottom: 2rem; border: 1px solid #e9ecef; }\n");
        html.append(".filter-row { display: flex; gap: 20px; flex-wrap: wrap; align-items: center; }\n");
        html.append(".filter-group { display: flex; flex-direction: column; gap: 5px; }\n");
        html.append(".filter-group label { font-weight: bold; font-size: 0.9em; color: #555; }\n");
        html.append(".filter-group select { appearance: none; background-color: #fff; background-image: url(\"data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%23007CB2%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E\"); background-repeat: no-repeat; background-position: right .7em top 50%; background-size: .65em auto; padding: 8px 12px; padding-right: 30px; border-radius: 4px; border: 1px solid #ced4da; font-size: 0.95em; color: #495057; transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out; cursor: pointer; }\n");
        html.append(".filter-group select:focus { border-color: #80bdff; outline: 0; box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25); }\n");
        html.append(".filter-group select:hover { border-color: #adb5bd; }\n");
        html.append(".checkbox-group { display: flex; gap: 15px; align-items: center; }\n");
        html.append(".checkbox-item { display: flex; align-items: center; gap: 5px; }\n");
        html.append(".hidden { display: none !important; }\n");
        
        // Logo CSS
        html.append("h1 img { vertical-align: middle; height: 1.5em; margin-right: 10px; }\n");

        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<a id=\"top\"></a>\n");
        html.append("<div class=\"container\">\n");
        html.append("<h1><img src=\"https://github.com/se2p/LitterBox/blob/master/src/main/resources/litterbox.png?raw=true\" alt=\"LitterBox Logo\">Code Report</h1>\n");

        int spriteCount = (int) new SpriteCount<>().calculateMetric(program);
        int scriptCount = (int) new ScriptCount<>().calculateMetric(program);
        int blockCount = (int) new BlockCount<>().calculateMetric(program);
        double ctScore = new ComputationalThinkingScore().calculateMetric(program);

        long bugCount = issues.stream().filter(i -> "BUG".equals(i.getIssueType().name())).count();
        long smellCount = issues.stream().filter(i -> "SMELL".equals(i.getIssueType().name())).count();
        long perfumeCount = issues.stream().filter(i -> "PERFUME".equals(i.getIssueType().name())).count();

        html.append("<div class=\"stats-container\">\n");
        html.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(spriteCount).append("</span><span class=\"stat-label\">Sprites</span></div>\n");
        html.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(scriptCount).append("</span><span class=\"stat-label\">Scripts</span></div>\n");
        html.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(blockCount).append("</span><span class=\"stat-label\">Blocks</span></div>\n");
        html.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(bugCount).append("</span><span class=\"stat-label\">Bugs</span></div>\n");
        html.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(smellCount).append("</span><span class=\"stat-label\">Smells</span></div>\n");
        html.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(perfumeCount).append("</span><span class=\"stat-label\">Perfumes</span></div>\n");
        html.append("<div class=\"stat-box\"><span class=\"stat-value\">").append(String.format("%.1f", ctScore)).append("</span><span class=\"stat-label\">CT Score</span></div>\n");
        html.append("</div>\n");

        if (issues.isEmpty()) {
            html.append("<p style=\"text-align: center; color: #28a745;\">No issues found!</p>\n");
        } else {
            
            // Filter UI
            html.append("<div id=\"filter-ui\" class=\"filter-container\">\n");
            html.append("<div class=\"filter-row\">\n");
            
            // Type checkboxes
            html.append("<div class=\"filter-group\">\n");
            html.append("<label>").append(translator.getInfo("filter_by_type")).append("</label>\n");
            html.append("<div class=\"checkbox-group\">\n");
            html.append("<div class=\"checkbox-item\"><input type=\"checkbox\" id=\"filter-bug\" checked onchange=\"applyFilters()\"> <label for=\"filter-bug\">").append(translator.getInfo("filter_bugs")).append("</label></div>\n");
            html.append("<div class=\"checkbox-item\"><input type=\"checkbox\" id=\"filter-smell\" checked onchange=\"applyFilters()\"> <label for=\"filter-smell\">").append(translator.getInfo("filter_smells")).append("</label></div>\n");
            html.append("<div class=\"checkbox-item\"><input type=\"checkbox\" id=\"filter-perfume\" checked onchange=\"applyFilters()\"> <label for=\"filter-perfume\">").append(translator.getInfo("filter_perfumes")).append("</label></div>\n");
            html.append("</div>\n"); // checkbox-group
            html.append("</div>\n"); // filter-group

            // Finder dropdown
            html.append("<div class=\"filter-group\">\n");
            html.append("<label for=\"finder-select\">").append(translator.getInfo("filter_by_finder")).append("</label>\n");
            html.append("<select id=\"finder-select\" onchange=\"applyFilters()\">\n");
            html.append("<option value=\"all\">").append(translator.getInfo("filter_all_finders")).append("</option>\n");
            html.append("</select>\n");
            html.append("</div>\n");

            // Sprite dropdown
            html.append("<div class=\"filter-group\">\n");
            html.append("<label for=\"sprite-select\">").append(translator.getInfo("filter_by_sprite")).append("</label>\n");
            html.append("<select id=\"sprite-select\" onchange=\"applyFilters()\">\n");
            html.append("<option value=\"all\">").append(translator.getInfo("filter_all_sprites")).append("</option>\n");
            html.append("</select>\n");
            html.append("</div>\n");

            html.append("</div>\n"); // filter-row
            html.append("</div>\n"); // filter-container


            Map<String, List<Issue>> issuesByCategory = issues.stream()
                    .collect(Collectors.groupingBy(this::getCategory));

            // Defined order of categories
            List<String> categories = List.of(FinderGroup.BUGS.group(), FinderGroup.SMELLS.group(), FinderGroup.PERFUMES.group());

            // Navigation
            html.append("<nav class=\"report-nav\"><ul>\n");
            for (String category : categories) {
                if (issuesByCategory.containsKey(category)) {
                    html.append("<li id=\"nav-").append(category).append("\"><a href=\"#").append(category).append("\">").append(translator.getInfo(category)).append("</a></li>\n");
                }
            }
            html.append("</ul></nav>\n");

            for (String category : categories) {
                if (issuesByCategory.containsKey(category)) {
                    html.append("<div id=\"section-").append(category).append("\" class=\"issue-section\">\n");
                    html.append("<h2 id=\"").append(category).append("\">").append(translator.getInfo(category)).append("</h2>\n");
                    
                    // Render description
                    String descriptionKey = "description_" + category;
                    try {
                        String description = translator.getInfo(descriptionKey);
                        if (description != null && !description.isEmpty() && !description.startsWith("Missing key")) {
                             html.append("<p class=\"category-description\">").append(StringEscapeUtils.escapeHtml4(description)).append("</p>\n");
                        }
                    } catch (Exception e) {
                        // Ignore missing descriptions
                    }
                    
                    renderIssueGroup(html, issuesByCategory.get(category));
                    html.append("<div class=\"back-to-top\"><a href=\"#top\">\u2191 ").append(translator.getInfo("back_to_top")).append("</a></div>\n");
                    html.append("</div>\n"); // end section
                }
            }

        }

        html.append("</div>\n");
        html.append("<script>\n");
        
        // Scratchblocks rendering
        html.append("scratchblocks.renderMatching('pre.blocks', {\n");
        html.append("  style: 'scratch3',\n");
        html.append("  scale: 0.7,\n");
        html.append("  languages: ['").append(translator.getLocale().getLanguage()).append("', 'en']\n");
        html.append("});\n");
        html.append("scratchblocks.renderMatching('code.scratchblocks', {\n");
        html.append("  style: 'scratch3',\n");
        html.append("  inline: true,\n");
        html.append("  scale: 0.7,\n");
        html.append("  languages: ['").append(translator.getLocale().getLanguage()).append("', 'en']\n");
        html.append("});\n");

        // Filtering Logic
        html.append("\n");
        html.append("function applyFilters() {\n");
        html.append("  const showBug = document.getElementById('filter-bug').checked;\n");
        html.append("  const showSmell = document.getElementById('filter-smell').checked;\n");
        html.append("  const showPerfume = document.getElementById('filter-perfume').checked;\n");
        html.append("  const selectedFinder = document.getElementById('finder-select').value;\n");
        html.append("  const selectedSprite = document.getElementById('sprite-select').value;\n");
        html.append("\n");
        html.append("  const issues = document.querySelectorAll('.issue');\n");
        html.append("  issues.forEach(issue => {\n");
        html.append("    const type = issue.getAttribute('data-type');\n");
        html.append("    const finder = issue.getAttribute('data-finder');\n");
        html.append("    const sprite = issue.getAttribute('data-sprite');\n");
        html.append("\n");
        html.append("    let visible = true;\n");
        html.append("\n");
        html.append("    // Type filter\n");
        html.append("    if (type === 'bugs' && !showBug) visible = false;\n");
        html.append("    if (type === 'smells' && !showSmell) visible = false;\n");
        html.append("    if (type === 'perfumes' && !showPerfume) visible = false;\n");
        html.append("\n");
        html.append("    // Finder filter\n");
        html.append("    if (selectedFinder !== 'all' && finder !== selectedFinder) visible = false;\n");
        html.append("\n");
        html.append("    // Sprite filter\n");
        html.append("    if (selectedSprite !== 'all' && sprite !== selectedSprite) visible = false;\n");
        html.append("\n");
        html.append("    if (visible) {\n");
        html.append("      issue.classList.remove('hidden');\n");
        html.append("    } else {\n");
        html.append("      issue.classList.add('hidden');\n");
        html.append("    }\n");
        html.append("  });\n");
        html.append("\n");
        html.append("  updateSectionVisibility();\n");
        html.append("}\n");
        html.append("\n");
        html.append("function updateSectionVisibility() {\n");
        html.append("  const sections = document.querySelectorAll('.issue-section');\n");
        html.append("  sections.forEach(section => {\n");
        html.append("    const visibleIssues = section.querySelectorAll('.issue:not(.hidden)');\n");
        html.append("    const sectionId = section.id.replace('section-', '');\n");
        html.append("    const navItem = document.getElementById('nav-' + sectionId);\n");
        html.append("\n");
        html.append("    if (visibleIssues.length > 0) {\n");
        html.append("      section.classList.remove('hidden');\n");
        html.append("      if (navItem) navItem.classList.remove('hidden');\n");
        html.append("    } else {\n");
        html.append("      section.classList.add('hidden');\n");
        html.append("      if (navItem) navItem.classList.add('hidden');\n");
        html.append("    }\n");
        html.append("  });\n");
        html.append("}\n");
        html.append("\n");
        html.append("function populateFilters() {\n");
        html.append("  const issues = document.querySelectorAll('.issue');\n");
        html.append("  const finders = new Set();\n");
        html.append("  const sprites = new Set();\n");
        html.append("\n");
        html.append("  issues.forEach(issue => {\n");
        html.append("    const finder = issue.getAttribute('data-finder');\n");
        html.append("    const sprite = issue.getAttribute('data-sprite');\n");
        html.append("    if (finder) finders.add(finder);\n");
        html.append("    if (sprite) sprites.add(sprite);\n");
        html.append("  });\n");
        html.append("\n");
        html.append("  const finderSelect = document.getElementById('finder-select');\n");
        html.append("  Array.from(finders).sort().forEach(finder => {\n");
        html.append("    const option = document.createElement('option');\n");
        html.append("    option.value = finder;\n");
        html.append("    option.textContent = finder;\n");
        html.append("    finderSelect.appendChild(option);\n");
        html.append("  });\n");
        html.append("\n");
        html.append("  const spriteSelect = document.getElementById('sprite-select');\n");
        html.append("  Array.from(sprites).sort().forEach(sprite => {\n");
        html.append("    const option = document.createElement('option');\n");
        html.append("    option.value = sprite;\n");
        html.append("    option.textContent = sprite;\n");
        html.append("    spriteSelect.appendChild(option);\n");
        html.append("  });\n");
        html.append("}\n");
        html.append("\n");
        html.append("document.addEventListener('DOMContentLoaded', populateFilters);\n");
        
        html.append("</script>\n");
        html.append("</body>\n");
        html.append("</html>");

        outputStream.write(html.toString().getBytes(StandardCharsets.UTF_8));
        if (closeStream) {
            outputStream.close();
        }
    }

    private String getCategory(Issue issue) {
        // Simple heuristic based on known groupings or IssueType if available
        // Assuming we can map issue types/finders to categories.
        // For now, let's rely on FinderGroup semantics if possible, or string matching.
        // Ideally `Issue` or `IssueFinder` has this info.
        // Checking `IssueTool` or similar would be better, but `Issue` usually knows its "type" via `IssueType`.
        // However `IssueType` is an enum like `BUG`, `SMELL`, etc.
        if (issue.getIssueType().name().equals("BUG")) {
            return FinderGroup.BUGS.group();
        } else if (issue.getIssueType().name().equals("SMELL")) {
            return FinderGroup.SMELLS.group();
        } else if (issue.getIssueType().name().equals("PERFUME")) {
            return FinderGroup.PERFUMES.group();
        }
        return "questions"; // Fallback
    }

    private void renderIssueGroup(StringBuilder html, List<Issue> issues) {
        Map<String, List<Issue>> issuesByFinder = issues.stream()
                .collect(Collectors.groupingBy(Issue::getFinderName));

        for (Map.Entry<String, List<Issue>> entry : issuesByFinder.entrySet()) {
            for (Issue issue : entry.getValue()) {
                renderIssue(html, issue);
            }
        }
    }

    private void renderIssue(StringBuilder html, Issue issue) {
        String category = getCategory(issue);
        String finderName = StringEscapeUtils.escapeHtml4(issue.getTranslatedFinderName(translator)); // For display
        // Use untranslated finder name for filtering to avoid issues with special logic or changes
        // Or better yet, allow filtering by the display name so it matches the dropdown. 
        // Let's use the display name for the data attribute for simplicity in the UI matching.
        String finderVal = finderName;
        
        String spriteName = "Stage";
        if (issue.getActorName() != null && !issue.getActorName().isEmpty()) {
            spriteName = StringEscapeUtils.escapeHtml4(issue.getActorName());
        }

        html.append("<div class=\"issue\" data-type=\"").append(category).append("\" data-finder=\"").append(finderVal).append("\" data-sprite=\"").append(spriteName).append("\">\n");
        html.append("<div class=\"issue-header\">\n");
        
        String title = finderName;
        if (issue.getActorName() != null && !issue.getActorName().isEmpty()) {
            title += " in " + translator.getInfo("sprite") + " " + spriteName;
        }
        
        html.append("<span class=\"issue-title\">").append(title).append("</span>");
        html.append("<span class=\"severity severity-").append(issue.getSeverity().name()).append("\">").append(issue.getSeverity().name()).append("</span> ");
        html.append("</div>\n");
        
        // Removed unnecessary metadata


        html.append("<div class=\"hint\">\n");
        html.append(formatHint(issue.getHintText(translator)));
        html.append("</div>\n");

        String scratchCode;
         if (issue instanceof MultiBlockIssue multi) {
            scratchCode = getScratchBlocksCode(issue, multi.getScriptEntities());
        } else {
            scratchCode = getScratchBlocksCode(issue, issue.getScriptOrProcedureDefinition());
        }

        if (scratchCode != null && !scratchCode.isEmpty()) {
            html.append("<pre class=\"blocks\">");
            // Remove [scratchblocks] tags since we put it in pre tag directly
            String cleanCode = scratchCode.replace("[scratchblocks]", "").replace("[/scratchblocks]", "").trim();
            html.append(StringEscapeUtils.escapeHtml4(cleanCode));
            html.append("</pre>\n");
        }

        html.append("</div>\n");
    }

    private String formatHint(String hint) {
        String escaped = StringEscapeUtils.escapeHtml4(hint);
        
        // Restore tags that we want to process
        escaped = escaped.replace("&lt;", "<").replace("&gt;", ">")
                .replace("[b]", "<b>").replace("[/b]", "</b>")
                .replace("[i]", "<i>").replace("[/i]", "</i>")
                .replace("[u]", "<u>").replace("[/u]", "</u>")
                .replace("[s]", "<s>").replace("[/s]", "</s>")
                .replace("[bc]", "<b><code>").replace("[/bc]", "</code></b>")
                .replace("[newLine]", "<br>")
                .replace("[LEQ]", "<code>&lt;</code>")
                .replace("[GEQ]", "<code>&gt;</code>")
                .replace("[EQ]", "<code>=</code>");

        // Handle translated tokens
        escaped = replaceToken(escaped, "[TRUE]", "true");
        escaped = replaceToken(escaped, "[FALSE]", "false");
        escaped = replaceToken(escaped, "[IF]", "if");
        escaped = replaceToken(escaped, "[ELSE]", "else");

        // Handle Scratchblocks specific tags
        escaped = escaped.replaceAll("\\[sbi\\](.*?)\\[/sbi\\]", "<code class=\"scratchblocks\">$1</code>");
        escaped = escaped.replaceAll("\\[var\\](.*?)\\[/var\\]", "<code class=\"scratchblocks\">($1)</code>"); // Variable shape (round)
        escaped = escaped.replaceAll("\\[list\\](.*?)\\[/list\\]", "<code class=\"scratchblocks\">($1 :: list)</code>"); // List shape (usually variable shape but we can distinguish if needed, lists in scratchblocks often just variable blocks)

        return escaped;
    }

    private String replaceToken(String text, String tag, String key) {
        if (text.contains(tag)) {
            String translated = translator.getInfo(key); // Assuming key exists in GeneralTerms
            return text.replace(tag, "<code>" + StringEscapeUtils.escapeHtml4(translated) + "</code>");
        }
        return text;
    }

    private String getIssueLocation(final Issue issue) {
        if (issue.getCodeLocation() == null) {
            return null;
        } else {
            return AstNodeUtil.getBlockId(issue.getCodeLocation());
        }
    }

    private String getScratchBlocksCode(final Issue issue, final ScriptEntity location) {
        if (location == null) {
            return "";
        } else {
            final ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor(issue);
            blockVisitor.begin();
            location.accept(blockVisitor);
            blockVisitor.end();
            return blockVisitor.getScratchBlocks();
        }
    }
    
    private String getScratchBlocksCode(final Issue issue, final List<ScriptEntity> locations) {
        StringBuilder builder = new StringBuilder();
        for (ScriptEntity location : locations) {
            if (location != null) {
                ScratchBlocksVisitor blockVisitor = new ScratchBlocksVisitor(issue);
                blockVisitor.begin();
                location.accept(blockVisitor);
                blockVisitor.end();
                String scratchBlockCode = blockVisitor.getScratchBlocks();
                builder.append(scratchBlockCode);
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
