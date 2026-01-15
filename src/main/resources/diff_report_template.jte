@import de.uni_passau.fim.se2.litterbox.diff.ProjectCodeDiff
@import de.uni_passau.fim.se2.litterbox.diff.ActorCodeDiff
@import de.uni_passau.fim.se2.litterbox.diff.ScriptCodeDiff
@import de.uni_passau.fim.se2.litterbox.diff.ScriptChangeType
@import de.uni_passau.fim.se2.litterbox.report.HTMLDiffReportGenerator.DiffTemplateModel
@import de.uni_passau.fim.se2.litterbox.report.HTMLDiffReportGenerator.DiffCategory
@import de.uni_passau.fim.se2.litterbox.report.HTMLDiffReportGenerator.RenderedDiffIssue

@param DiffTemplateModel model


<!--
Copyright (C) 2019-2026 LitterBox contributors

This file is part of LitterBox.

LitterBox is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at
your option) any later version.

LitterBox is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
-->
<!DOCTYPE html>
<html lang="${model.language()}">
<head>
    <meta charset="UTF-8">
    <title>LitterBox Diff Report</title>
    <link rel="icon" href="${model.favicon()}">
    <script>$unsafe{model.scripts().scratchBlocks()}</script>
    <script>$unsafe{model.scripts().scratchBlocksTranslations()}</script>
    <style>
        body {
            font-family: sans-serif;
            background-color: #f4f4f4;
            color: #333;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }

        .boxed {
            margin-bottom: 1rem;
            background: #fff;
            padding: 1rem;
            border-radius: 8px;
            box-shadow: 0 1px 5px rgba(0, 0, 0, 0.08);
        }

        .report-header {
            text-align: center;
        }

        .logo {
            display: inline-block;
            vertical-align: middle;
            margin-right: 1em;
        }

        .logo > svg {
            height: 5em;
            vertical-align: middle;
            margin-bottom: 1em;
        }

        .report-header h1 {
            display: inline-block;
            vertical-align: middle;
            margin: 0;
            color: #333;
            font-size: 1.8em;
        }

        .comparison-info {
            text-align: center;
            margin: 1rem 0;
            font-size: 1.1em;
            color: #555;
        }

        .comparison-info .arrow {
            margin: 0 0.5em;
            color: #007bff;
        }

        /* Summary stats */
        .stats-container {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 20px;
            margin-bottom: 2rem;
            background: #fff;
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 1px 5px rgba(0, 0, 0, 0.08);
        }

        .stat-box {
            text-align: center;
            min-width: 100px;
        }

        .stat-value {
            font-size: 2em;
            font-weight: bold;
            display: block;
        }

        .stat-label {
            font-size: 0.9em;
            color: #777;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .stat-fixed .stat-value { color: #28a745; }
        .stat-new .stat-value { color: #dc3545; }
        .stat-persisting .stat-value { color: #6c757d; }
        .stat-removed .stat-value { color: #ffc107; }

        /* Navigation bar */
        .report-nav {
            text-align: center;
            margin-bottom: 1.5rem;
        }

        .report-nav > ul {
            padding-left: 0;
            margin: 0;
        }

        .report-nav li {
            display: inline;
        }

        .report-nav a {
            text-decoration: none;
            color: #555;
            font-weight: 500;
            font-size: large;
            padding: 8px 16px;
            border-radius: 20px;
            transition: all 0.2s ease;
            display: inline-block;
            background: #f8f9fa;
            margin: 4px;
        }

        .report-nav a:hover {
            background-color: #e9ecef;
            color: #000;
        }

        .report-nav a.nav-fixed { border-left: 3px solid #28a745; }
        .report-nav a.nav-new { border-left: 3px solid #dc3545; }
        .report-nav a.nav-removed { border-left: 3px solid #ffc107; }

        .report-nav a.nav-code-changes {
            border-left: 3px solid #17a2b8;
        }

        /* Category sections */
        .category-section {
            margin-bottom: 2rem;
        }

        .category-header {
            cursor: pointer;
            display: flex;
            align-items: center;
            user-select: none;
            padding: 1rem;
            background: #fff;
            border-radius: 8px 8px 0 0;
            margin-bottom: 0;
            border: 1px solid #e9ecef;
            border-bottom: none;
        }

        .category-header:hover {
            background-color: #f8f9fa;
        }

        .toggle-icon {
            margin-right: 10px;
            transition: transform 0.3s ease;
            font-size: 0.8em;
            color: #777;
        }

        .category-section.collapsed .toggle-icon {
            transform: rotate(-90deg);
        }

        .category-section.collapsed .category-content {
            display: none;
        }

        .category-title {
            font-size: 1.3em;
            font-weight: 600;
            margin: 0;
        }

        .category-marker {
            font-size: 1.2em;
            margin-right: 10px;
            width: 25px;
            text-align: center;
        }

        .category-count {
            margin-left: auto;
            background: #e9ecef;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.9em;
            font-weight: 600;
        }

        .category-new .category-marker { color: #dc3545; }
        .category-new .category-count { background: #f8d7da; color: #721c24; }

        .category-fixed .category-marker { color: #28a745; }
        .category-fixed .category-count { background: #d4edda; color: #155724; }

        .category-persisting .category-marker { color: #6c757d; }
        .category-persisting .category-count { background: #e9ecef; color: #495057; }

        .category-removed .category-marker { color: #ffc107; }
        .category-removed .category-count { background: #fff3cd; color: #856404; }

        .category-content {
            background: #fff;
            border: 1px solid #e9ecef;
            border-top: none;
            border-radius: 0 0 8px 8px;
            padding: 1rem;
        }

        /* Issues */
        .issue {
            border: 1px solid #ddd;
            border-radius: 6px;
            padding: 1.2rem;
            margin-bottom: 1.2rem;
            background: #fff;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }

        .issue:last-child {
            margin-bottom: 0;
        }

        .issue-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 10px;
            margin-bottom: 0.8rem;
        }

        .issue-title {
            font-size: 1.15em;
            font-weight: 600;
            color: #333;
        }

        .issue-actor {
            font-size: 1em;
            font-weight: 600;
            color: #333;
        }

        .hint {
            background: #f1f3f5;
            padding: 1rem;
            border-radius: 4px;
            margin-bottom: 1rem;
            border-left: 4px solid #17a2b8;
        }

        pre.blocks {
            background: #fff;
            border: 1px solid #eee;
            padding: 10px;
            border-radius: 4px;
            overflow-x: auto;
            margin: 0;
        }

        /* Side-by-side code diff */
        .code-diff {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1rem;
            margin-top: 1rem;
        }

        .code-diff.single-panel {
            grid-template-columns: 1fr;
        }

        .diff-panel {
            border-radius: 6px;
            overflow: hidden;
        }

        .diff-panel-header {
            padding: 0.5rem 1rem;
            font-weight: 600;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .diff-panel-before {
            border: 2px solid #dc3545;
        }

        .diff-panel-before .diff-panel-header {
            background: #f8d7da;
            color: #721c24;
        }

        .diff-panel-after {
            border: 2px solid #28a745;
        }

        .diff-panel-after .diff-panel-header {
            background: #d4edda;
            color: #155724;
        }

        .diff-panel-unchanged {
            border: 2px solid #e0e0e0;
        }

        .diff-panel-unchanged .diff-panel-header {
            background: #f0f0f0;
            color: #555;
        }

        .diff-panel-after-failure {
            border: 2px solid #dc3545;
        }

        .diff-panel-after-failure .diff-panel-header {
            background: #f8d7da;
            color: #721c24;
        }

        .diff-panel pre.blocks {
            border: none;
            border-radius: 0;
            margin: 0;
        }

        code.scratchblocks {
            font-family: sans-serif;
            font-weight: bold;
            padding: 2px 4px;
            border-radius: 4px;
        }

        .diff-panel-before, .diff-panel-after {
            position: relative;
        }

        .diff-panel pre.blocks {
            padding-left: 45px; /* Space for emojis */
            overflow: visible; /* Allow emojis to hang outside parent if needed */
        }

        .emoji-gutter-marker {
            font-size: 1.2em;
            pointer-events: none;
        }



        /* Severity badges */
        .severity {
            font-size: 0.65em;
            padding: 2px 6px;
            border-radius: 12px;
            color: #fff;
            font-weight: bold;
            text-transform: uppercase;
        }

        .severity-HIGH { background-color: #dc3545; }
        .severity-MEDIUM { background-color: #ffc107; color: #000; }
        .severity-LOW { background-color: #17a2b8; }
        .severity-perfume-HIGH { background-color: #198754; }
        .severity-perfume-MEDIUM { background-color: #28a745; }
        .severity-perfume-LOW { background-color: #A3CFBB; color: #000; }

        .severity-icon {
            font-size: 0.5em;
            vertical-align: middle;
        }

        footer {
            margin-top: 3rem;
            border-top: 1px solid #ddd;
            padding-top: 1rem;
            color: #777;
            font-size: 0.85em;
        }

        .hidden {
            display: none !important;
        }

        #back-to-top {
            position: fixed;
            bottom: 20px;
            right: 40px;
            max-width: 30px;
            border-color: rgba(85, 85, 85, 0.2);
            background-color: rgb(100, 100, 100);
            color: white;
            padding: 1px 5px;
            border-radius: 4px;
            font-size: 18pt;
            text-decoration: none;
        }
    </style>
</head>
<body>
<a id="top"></a>
<header class="report-header boxed">
    <div class="logo">
        $unsafe{model.logo()}
    </div>
    <h1>Bug Diff Report</h1>
</header>

<div class="comparison-info boxed">
    <strong>${model.beforeSource()}</strong>
    <span class="arrow">→</span>
    <strong>${model.afterSource()}</strong>
</div>

<div class="stats-container">
    <div class="stat-box stat-fixed">
        <span class="stat-value">${model.summary().fixed()}</span>
        <span class="stat-label">${model.translator().getInfo("diff_fixed")}</span>
    </div>
    <div class="stat-box stat-new">
        <span class="stat-value">${model.summary().newBugs()}</span>
        <span class="stat-label">${model.translator().getInfo("diff_new")}</span>
    </div>
    <div class="stat-box stat-persisting">
        <span class="stat-value">${model.summary().persisting()}</span>
        <span class="stat-label">${model.translator().getInfo("diff_persisting")}</span>
    </div>
    <div class="stat-box stat-removed">
        <span class="stat-value">${model.summary().removed()}</span>
        <span class="stat-label">${model.translator().getInfo("diff_removed")}</span>
    </div>
</div>

@if (!model.categories().isEmpty())
<nav class="report-nav boxed">
    <ul>
        @for (DiffCategory category : model.categories())
            <li>
                <a href="#section-${category.id()}" class="nav-${category.id().toLowerCase()}">
                    ${model.translator().getInfo(category.labelKey())}
                </a>
            </li>
        @endfor
        @if (!model.projectCodeDiff().isEmpty())
            <li>
                <a href="#section-code-changes" class="nav-code-changes">
                    ${model.translator().getInfo("diff_code_changes")}
                </a>
            </li>
        @endif
    </ul>
</nav>
@endif

@if (model.categories().isEmpty())
    <div class="boxed" style="text-align: center; color: #28a745;">
        ${model.translator().getInfo("no_issues_found")}
    </div>
@else
    @for (DiffCategory category : model.categories())
        <div class="category-section ${category.cssClass()}" id="section-${category.id()}">
            <div class="category-header" onclick="toggleCategory('${category.id()}')">
                <span class="toggle-icon">▼</span>
                <span class="category-title">${model.translator().getInfo(category.labelKey())}</span>
                <span class="category-count">${category.issues().size()}</span>
            </div>
            <div class="category-content" id="content-${category.id()}">
                @for (RenderedDiffIssue issue : category.issues())
                    <div class="issue"
                         data-finder="${issue.finder()}"
                         data-sprite="${issue.sprite()}">
                        <div class="issue-header">
                            <span class="issue-title">
                                ${issue.translatedFinderName()}
                                <span class="issue-actor">in ${issue.sprite() != null ? issue.sprite() : "Stage"}</span>
                            </span>
                            <span class="severity ${issue.severityClass()}">
                                <span class="severity-icon">$unsafe{issue.severityIcon()}</span>
                                ${model.translator().getInfo("severity_" + issue.severity())}
                            </span>
                        </div>
                        <div class="hint">
                            $unsafe{issue.hintText()}
                        </div>
                        @if (issue.isCodeUnchanged())
                            <div class="code-diff single-panel">
                                <div class="diff-panel diff-panel-unchanged">
                                    <div class="diff-panel-header">${model.translator().getInfo("diff_unchanged")}</div>
                                    <pre class="blocks">
${issue.afterScratchBlocks()}</pre>
                                </div>
                            </div>
                        @elseif ((issue.beforeScratchBlocks() != null && !issue.beforeScratchBlocks().isEmpty()) || (issue.afterScratchBlocks() != null && !issue.afterScratchBlocks().isEmpty()))
                            !{var hasBefore = issue.beforeScratchBlocks() != null && !issue.beforeScratchBlocks().isEmpty();}
                            !{var hasAfter = issue.afterScratchBlocks() != null && !issue.afterScratchBlocks().isEmpty();}
                            !{var showBothPanels = hasBefore && hasAfter && (issue.categoryId().equals("FIXED") || issue.categoryId().equals("NEW") || issue.categoryId().equals("PERSISTING"));}
                            <div class="code-diff${showBothPanels ? "" : " single-panel"}">
                                @if (hasBefore && (issue.categoryId().equals("FIXED") || issue.categoryId().equals("REMOVED") || showBothPanels))
                                    <div class="diff-panel diff-panel-before">
                                        <div class="diff-panel-header">${model.translator().getInfo("diff_before")}</div>
                                        <pre class="blocks">
${issue.beforeScratchBlocks()}</pre>
                                    </div>
                                @endif
                                @if (hasAfter && (issue.categoryId().equals("NEW") || issue.categoryId().equals("PERSISTING") || showBothPanels))
                                    !{String afterPanelClass = issue.categoryId().equals("FIXED") ? "diff-panel-after" : "diff-panel-after-failure";}
                                    <div class="diff-panel ${afterPanelClass}">
                                        <div class="diff-panel-header">${model.translator().getInfo("diff_after")}</div>
                                        <pre class="blocks">
${issue.afterScratchBlocks()}</pre>
                                    </div>
                                @endif
                            </div>
                        @endif
                    </div>
                @endfor
            </div>
        </div>
    @endfor
@endif

@if (!model.projectCodeDiff().isEmpty())
    <div class="category-section category-code-changes" id="section-code-changes">
        <div class="category-header" onclick="toggleCategory('code-changes')">
            <span class="toggle-icon">▼</span>
            <span class="category-title">${model.translator().getInfo("diff_code_changes")}</span>
        </div>
        <div class="category-content" id="content-code-changes">
            @for (ActorCodeDiff actor : model.projectCodeDiff().actorDiffs())
                @if (actor.hasResultingDiffs())
                    <div class="issue" style="margin-bottom: 1.5rem;">
                        <div class="issue-header"
                             style="background: #f8f9fa; border-radius: 6px; padding: 0.75rem 1rem; margin-bottom: 1rem;">
                            <span class="issue-title" style="font-size: 1.2em;">
                                ${actor.actorName()} ${actor.isStage() ? "(Stage)" : ""}
                            </span>
                        </div>
                        @for (ScriptCodeDiff script : actor.scriptDiffs())
                            @if (!script.isUnchanged())
                                !{boolean isAdded = script.changeType() == ScriptChangeType.ADDED;}
                                !{boolean isDeleted = script.changeType() == ScriptChangeType.DELETED;}
                                !{boolean isChanged = script.changeType() == ScriptChangeType.CHANGED;}
                                <div style="margin-bottom: 1rem; padding: 0.5rem; border: 1px solid #eee; border-radius: 6px;">
                                    <div style="margin-bottom: 0.5rem;">
                                        @if (isAdded)
                                            <span style="background: #d4edda; color: #155724; padding: 2px 8px; border-radius: 4px; font-size: 0.85em; font-weight: 600;">+ ${model.translator().getInfo("diff_added")}</span>
                                        @elseif (isDeleted)
                                            <span style="background: #f8d7da; color: #721c24; padding: 2px 8px; border-radius: 4px; font-size: 0.85em; font-weight: 600;">− ${model.translator().getInfo("diff_deleted")}</span>
                                        @elseif (isChanged)
                                            <span style="background: #fff3cd; color: #856404; padding: 2px 8px; border-radius: 4px; font-size: 0.85em; font-weight: 600;">↔ ${model.translator().getInfo("diff_changed")}</span>
                                        @endif
                                    </div>
                                    <div class="code-diff${isChanged ? "" : " single-panel"}">
                                        @if (isDeleted)
                                            <div class="diff-panel diff-panel-before">
                                                <div class="diff-panel-header">${model.translator().getInfo("diff_before")}</div>
                                                <pre class="blocks">
${script.beforeCode()}</pre>
                                            </div>
                                        @elseif (isAdded)
                                            <div class="diff-panel diff-panel-after">
                                                <div class="diff-panel-header">${model.translator().getInfo("diff_after")}</div>
                                                <pre class="blocks">
${script.afterCode()}</pre>
                                            </div>
                                        @elseif (isChanged)
                                            <div class="diff-panel diff-panel-before">
                                                <div class="diff-panel-header">${model.translator().getInfo("diff_before")}</div>
                                                <pre class="blocks">
${script.annotatedBeforeCode()}</pre>
                                            </div>
                                            <div class="diff-panel diff-panel-after">
                                                <div class="diff-panel-header">${model.translator().getInfo("diff_after")}</div>
                                                <pre class="blocks">
${script.annotatedAfterCode()}</pre>
                                            </div>
                                        @endif
                                    </div>
                                </div>
                            @endif
                        @endfor
                    </div>
                @endif
            @endfor
        </div>
    </div>
@endif



<a href="#top" id="back-to-top" title="${model.translator().getInfo("back_to_top")}">▲</a>

<footer>
    <p>This report includes third-party software:</p>
    <ul>
        <li>
            <strong>scratchblocks</strong> (MIT License) - <a href="https://github.com/scratchblocks/scratchblocks">GitHub</a>
        </li>
    </ul>
    <p>Generated by LitterBox.</p>
</footer>

<script>
    function toggleCategory(categoryId) {
        const section = document.getElementById('section-' + categoryId);
        if (section) {
            section.classList.toggle('collapsed');
        }
    }

    // ScratchBlocks rendering
    function renderScratchBlocks() {
        scratchblocks.renderMatching('pre.blocks', {
            style: 'scratch3',
            scale: 0.7,
            languages: ['${model.language()}', 'en']
        });
        scratchblocks.renderMatching('code.scratchblocks', {
            style: 'scratch3',
            inline: true,
            scale: 0.7,
            languages: ['${model.language()}', 'en']
        });
    }

    // Apply thick borders to tinted blocks
    function applyCustomBorders() {
        const blobs = document.querySelectorAll('svg path');
        blobs.forEach(blob => {
            const fill = blob.getAttribute('fill');
            if (fill) {
                // Check for red (removed) or green (added) tints
                const lowerFill = fill.toLowerCase();
                if (lowerFill === '#dc3545' || lowerFill === '#28a745') {
                    // Apply thick border with contrasting color
                    if (lowerFill === '#dc3545') {
                        blob.style.stroke = '#bd2130'; // Darker red
                    } else {
                        blob.style.stroke = '#1e7e34'; // Darker green
                    }
                    blob.style.strokeWidth = '3px';
                }
            }
        });
    }

    // Initialize on load
    window.addEventListener('load', function () {
        renderScratchBlocks();
        // Run immediately after rendering, no timeout needed as renderMatching is synchronous
        applyCustomBorders();
    });
</script>
</body>
</html>
