@import de.uni_passau.fim.se2.litterbox.analytics.IssueType
@import de.uni_passau.fim.se2.litterbox.report.HTMLReportGenerator
@import de.uni_passau.fim.se2.litterbox.report.HTMLReportGenerator.TemplateModel
@import de.uni_passau.fim.se2.litterbox.report.HTMLReportGenerator.ProjectModel
@import gg.jte.support.ForSupport

@param TemplateModel model

<!--
Copyright (C) 2019-2024 LitterBox contributors

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
    <title>LitterBox Report</title>
    <link rel="icon" href="${model.favicon()}">
    <script>$unsafe{model.scripts().scratchBlocks()}</script>
    <script>$unsafe{model.scripts().scratchBlocksTranslations()}</script>
    <script>$unsafe{model.scripts().graphviz()}</script>
    <script>$unsafe{model.scripts().svgPanZoom()}</script>
    <style>
        body {
            font-family: sans-serif;
            background-color: #f4f4f4;
            color: #333;
        }

        .boxed {
            margin-bottom: 1rem;
            background: #fff;
            padding: 1rem;
            border-radius: 8px;
            box-shadow: 0 1px 5px rgba(0, 0, 0, 0.08);
        }

        .with-sidebar {
            display: flex;
            flex-wrap: wrap;
            gap: 1rem;
        }

        .sidebar {
            flex-basis: 20rem;
            flex-grow: 1;
        }

        .main-content {
            flex-basis: 0;
            flex-grow: 999;
            min-inline-size: 50%;
        }

        .report-header {
            text-align: center;
        }

        .logo > svg {
            height: 3em;
            vertical-align: middle;
            margin-right: 15px;
        }

        .report-header h1 {
            display: inline-block;
            vertical-align: middle;
            margin: 0;
            color: #333;
            font-size: 1.8em;
        }

        footer {
            margin-top: 3rem;
            border-top: 1px solid #ddd;
            padding-top: 1rem;
            color: #777;
            font-size: 0.85em;
        }

        .active {
            font-weight: bold;
        }

        /* Graphs */
        .graphs-container {
            background: #fff;
            padding: 1.5rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            border: 1px solid #e9ecef;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }

        .graph-tabs {
            display: flex;
            border-bottom: 1px solid #ddd;
            margin-bottom: 1rem;
        }

        .graph-tab {
            padding: 10px 20px;
            cursor: pointer;
            border: 1px solid transparent;
            border-bottom: none;
            margin-bottom: -1px;
            border-radius: 4px 4px 0 0;
            color: #555;
            background: #f9f9f9;
        }

        .graph-tab:hover {
            background: #eee;
        }

        .graph-tab.active {
            background: #fff;
            border-color: #ddd;
            border-bottom-color: #fff;
            color: #007bff;
            font-weight: bold;
        }

        .graph-view {
            display: none;
            height: 600px;
            border: 1px solid #eee;
            border-radius: 0 0 4px 4px;
            overflow: hidden;
            position: relative;
        }

        .graph-view.active {
            display: block;
        }

        .graphviz-container {
            width: 100%;
            height: 100%;
        }

        .containered {
            margin-bottom: 2rem;
            background: #fff;
            padding: 1rem;
            border-radius: 8px;
            box-shadow: 0 1px 5px rgba(0, 0, 0, 0.08);
        }

        /* stats */
        .stats-container {
            display: flex;
            flex-wrap: wrap;
            justify-content: center;
            gap: 15px;
            margin-bottom: 2rem;
            background: #fff;
            padding: 1rem;
            border-radius: 8px;
            box-shadow: 0 1px 5px rgba(0, 0, 0, 0.08);
        }

        .stat-box {
            text-align: center;
            min-width: 80px;
        }

        .stat-value {
            font-size: 1.5em;
            font-weight: bold;
            color: #007bff;
            display: block;
        }

        .stat-label {
            font-size: 0.8em;
            color: #777;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        /* Metrics */
        .metrics-container {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 1.5rem;
        }

        .metric-item {
            background: #fff;
            border: 1px solid #ddd;
            border-radius: 6px;
            padding: 15px;
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
            transition: transform 0.2s;
        }

        .metric-item:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .metric-name {
            font-size: 0.9em;
            color: #666;
            margin-bottom: 5px;
            word-break: break-word;
        }

        .metric-value {
            font-size: 1.4em;
            font-weight: bold;
            color: #28a745;
        }

        /* issue severity */
        .severity {
            font-size: 0.65em;
            padding: 2px 6px;
            border-radius: 12px;
            color: #fff;
            font-weight: bold;
            text-transform: uppercase;
        }

        .severity-HIGH {
            background-color: #dc3545;
        }

        .severity-MEDIUM {
            background-color: #ffc107;
            color: #000;
        }

        .severity-LOW {
            background-color: #17a2b8;
        }

        .severity-perfume-HIGH {
            background-color: #198754;
        }

        .severity-perfume-MEDIUM {
            background-color: #28a745;
        }

        .severity-perfume-LOW {
            background-color: #A3CFBB;
            color: #000;
        }

        .severity-icon {
            font-size: 0.5em;
            vertical-align: middle;
        }

        /* Filter */
        .filter-container {
            background: #fff;
            padding: 1.5rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            border: 1px solid #e9ecef;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }

        .filter-row {
            display: flex;
            gap: 30px;
            flex-wrap: wrap;
            align-items: flex-start;
        }

        .filter-group {
            display: flex;
            flex-direction: column;
            gap: 8px;
        }

        .filter-group label {
            font-weight: 600;
            font-size: 0.9em;
            color: #444;
        }

        .filter-group select {
            appearance: none;
            background-color: #fff;
            background-image: url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%23007CB2%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E");
            background-repeat: no-repeat;
            background-position: right .7em top 50%;
            background-size: .65em auto;
            padding: 8px 32px 8px 12px;
            border-radius: 4px;
            border: 1px solid #ced4da;
            font-size: 0.95em;
            color: #495057;
            transition: border-color .15s ease-in-out, box-shadow .15s ease-in-out;
            cursor: pointer;
            min-width: 150px;
        }

        .filter-group select:focus {
            border-color: #80bdff;
            outline: 0;
            box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, .25);
        }

        .checkbox-group {
            display: flex;
            gap: 15px;
            align-items: center;
            padding-top: 5px;
        }

        .checkbox-item {
            display: flex;
            align-items: center;
            gap: 6px;
            cursor: pointer;
        }

        .checkbox-item label {
            font-weight: normal;
            cursor: pointer;
            margin-bottom: 0;
        }

        .hidden {
            display: none !important;
        }
    </style>
</head>
<body>
<header class="report-header boxed">
    <div class="logo">
        $unsafe{model.logo()}
    </div>
    <h1>Project <span id="project-name-header"></span></h1>
</header>

<main class="with-sidebar">
    <div class="sidebar boxed" style="${model.projects().size() <= 1 ? "display: none;" : null}">
        <ul>
            @for (var project : ForSupport.of(model.projects()))
                <li
                    id="nav-item-${project.get().id()}"
                    class="${project.isFirst() ? "project-nav-item active" : "project-nav-item"}"
                    onclick="selectProject('${project.get().id()}', '${project.get().name()}')">
                    ${project.get().name()}
                </li>
            @endfor
        </ul>
    </div>
    <div class="main-content boxed">
        <a id="top"></a>
        <div id="main-content-block"></div>
    </div>
</main>

<footer>
    <p>This report includes third-party software:</p>
    <ul>
        <li>
            <strong>scratchblocks</strong> (MIT License) - <a href="https://github.com/scratchblocks/scratchblocks">GitHub</a>
        </li>
        <li>
            <strong>Viz.js</strong> (MIT License) - <a href="https://github.com/mdaines/viz.js">GitHub</a>
        </li>
        <li>
            <strong>svg-pan-zoom</strong> (BSD 2-Clause License) - <a href="https://github.com/ariutta/svg-pan-zoom">GitHub</a>
        </li>
    </ul>
    <p>Generated by LitterBox.</p>
</footer>

@for (ProjectModel project : model.projects())
    <template id="project-template-${project.id()}">
        <details id="section-stats-${project.id()}" class="issue-section" open>
            <summary><strong>TODO: Translate Stats Section Title</strong></summary>
            <div class="stats-container">
                @for (var stat : project.stats())
                    <div class="stat-box">
                        <span class="stat-value">${stat.value()}</span>
                        <span class="stat-label">${stat.label()}</span>
                    </div>
                @endfor
            </div>
        </details>

        <nav class="report-nav">
            <ul>
                @for (String type : project.issues().stream().map(HTMLReportGenerator.IssueGroup::category).distinct().sorted().toList())
                    <li id="nav-${type}-${project.id()}">
                        <a href="#section-${type}-${project.id()}">${model.translator().getInfo(type)}</a>
                    </li>
                @endfor
                <li id="nav-graphs-${project.id()}">
                    <a href="#section-graphs-${project.id()}">${model.translator().getInfo("label_graphs")}</a>
                </li>
                <li id="nav-metrics-${project.id()}">
                    <a href="#section-metrics-${project.id()}">${model.translator().getInfo("label_metrics")}</a>
                </li>
            </ul>
        </nav>

        <details id="section-issues-${project.id()}" class="issue-section" open>
            <summary><strong>TODO: Translate Issues Section Title</strong></summary>
            @if (project.issues().isEmpty())
                <p style="text-align: center; color: #28a745;">${model.translator().getInfo("no_issues_found")}</p>
            @else
                @for (HTMLReportGenerator.IssueGroup issueGroup : project.issues())
                    <details id="section-${issueGroup.category()}-${project.id()}" class="issue-section" open>
                        <summary
                                id="${issueGroup.category()}-${project.id()}"
                                class="collapsible-header"
                                onclick="toggleSection('${issueGroup.category()}-${project.id()}')"
                        >
                            ${model.translator().getInfo(issueGroup.category())}
                        </summary>
                        <div class="filter-container" id="filter-ui-${issueGroup.category()}-${project.id()}">
                            <div class="filter-row">
                                <!-- finder filter -->
                                <div class="filter-group">
                                    !{var selectIdFinder = "finder-select-" + issueGroup.category() + "-" + project.id();}
                                    <label for="${selectIdFinder}">${model.translator().getInfo("filter_by_finder")}</label>
                                    <select id="${selectIdFinder}" onchange="filterSection('${issueGroup.category()}', '${project.id()}')" class="finder-select">
                                        <option value="all">${model.translator().getInfo("filter_all_finders")}</option>
                                        @for (var entry : issueGroup.finders().entrySet())
                                            <option value="${entry.getKey()}">${entry.getValue()}</option>
                                        @endfor
                                    </select>
                                </div>
                                <!-- sprite filter -->
                                <div class="filter-group">
                                    !{var selectIdSprite = "sprite-select-" + issueGroup.category() + "-" + project.id();}
                                    <label for="${selectIdSprite}">${model.translator().getInfo("filter_by_sprite")}</label>
                                    <select id="${selectIdSprite}" onchange="filterSection('${issueGroup.category()}', '${project.id()}')" class="sprite-select">
                                        <option value="all">${model.translator().getInfo("filter_all_sprites")}</option>
                                        @for (var sprite : issueGroup.sprites())
                                            <option value="${sprite}">${sprite}</option>
                                        @endfor
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div id="content-${issueGroup.category()}-${project.id()}" class="containered">
                            <p class="category-description">${model.translator().getInfo("description_" + issueGroup.category())}</p>
                            @for (var issueEntry : issueGroup.issues().entrySet())
                                @for (HTMLReportGenerator.RenderedIssue issue : issueEntry.getValue())
                                    <div
                                            class="issue"
                                            data-type="${issue.category()}"
                                            data-finder="${issue.finder()}"
                                            data-sprite="${issue.sprite()}"
                                    >
                                        <div class="issue-header">
                                            <span class="issue-title">${issue.translatedFinderName()}</span>
                                            <span class="severity ${issue.severityClass()}">
                                                <span class="severity-icon">$unsafe{issue.severityIcon()}</span>
                                                ${model.translator().getInfo("severity_" + issue.severity())}
                                            </span>
                                        </div>
                                        <div class="hint">
                                            $unsafe{issue.hintText()}
                                        </div>
                                        @if (issue.scratchBlocks() != null && !issue.scratchBlocks().isEmpty())
                                            <pre class="blocks">
                                                $unsafe{issue.scratchBlocks()}
                                            </pre>
                                        @endif
                                        @if (issue.refactoredScript() != null && !issue.refactoredScript().isEmpty())
                                            <button class="refactoring-toggle" onclick="toggleRefactoring('${issue.id()}')">
                                                ${model.translator().getInfo("show_fix")}
                                            </button>
                                            <div id="refactoring-${issue.id()}" class="refactoring-content">
                                                <pre class="blocks">
                                                    $unsafe{issue.refactoredScript()}
                                                </pre>
                                            </div>
                                        @endif
                                    </div>
                                @endfor
                            @endfor
                        </div>
                    </details>
                @endfor
            @endif
        </details>

        <details id="section-graphs-${project.id()}" class="issue-section" open>
            <summary><strong>TODO: Translate Graphs Section Title</strong></summary>
            <div class="graphs-container">
                <div class="graph-tabs">
                @for (var graph : ForSupport.of(project.graphs()))
                    <div
                            id="graph-tab-${graph.get().type()}-${project.id()}"
                            class="${graph.isFirst() ? "graph-tab active" : "graph-tab"}"
                            onclick="showGraph('${graph.get().type()}', '${project.id()}')"
                    >
                        ${graph.get().type()}
                    </div>
                @endfor
                </div>

                @for (var graph : ForSupport.of(project.graphs()))
                <div id="graph-view-${graph.get().type()}-${project.id()}" class="graph-view">
                    <div id="graphviz-${graph.get().type()}-${project.id()}" class="graphviz-container"></div>
                </div>
                @endfor
            </div>
        </details>

        <details id="section-metrics-${project.id()}" class="issue-section" open>
            <summary><strong>TODO: Translate Metrics Section Title</strong></summary>
            <p class="category-description">${model.translator().getInfo("description_metrics")}</p>
            <div class="metrics-container">
                @for (var metric : project.metrics())
                    <div class="metric-item">
                        <span class="metric-name">${metric.name()}</span>
                        <span class="metric-value">${String.format("%.2f", metric.value())}</span>
                    </div>
                @endfor
            </div>
        </details>
    </template>
@endfor

<script>
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

    document.addEventListener('DOMContentLoaded', () => {
        renderScratchBlocks();
    });

    // project switching
    function selectProject(projectId, projectName) {
        document.querySelectorAll('.project-nav-item').forEach(el => el.classList.remove('active'));
        document.getElementById('nav-item-' + projectId).classList.add('active');
        document.getElementById('project-name-header').textContent = projectName;

        const template = document.getElementById('project-template-' + projectId);
        const clone = document.importNode(template.content, true);

        const mainBody = document.getElementById('main-content-block');
        mainBody.replaceChildren(clone)

        renderScratchBlocks();
        showGraph('CFG', projectId);
    }

    // graphs
    window.graphData = window.graphData || {};
    @for (var project : ForSupport.of(model.projects()))
        window.graphData['${project.get().id()}'] = {
            @for (var entry : project.get().graphs())
                '${entry.type()}': '${entry.dotGraph()}',
            @endfor
        }

        @if (project.isFirst())
            window.addEventListener('load', function () {
                showGraph('CFG', '${project.get().id()}')
            });
        @endif
    @endfor

    function showGraph(type, projectId) {
        // Update Tabs
        const tabs = document.querySelectorAll('#section-graphs-' + projectId + ' .graph-tab');
        tabs.forEach(el => el.classList.remove('active'));
        const activeTab = document.getElementById('graph-tab-' + type + '-' + projectId);
        if (activeTab) {
            activeTab.classList.add('active');
        }

        // Update Views
        const views = document.querySelectorAll('#section-graphs-' + projectId + ' .graph-view');
        views.forEach(el => el.classList.remove('active'));
        const activeView = document.getElementById('graph-view-' + type + '-' + projectId);
        if (activeView) {
            activeView.classList.add('active');
        }

        // Render Graph
        renderGraph(type, projectId);
    }

    function renderGraph(type, projectId) {
        if (!window.graphData[projectId] || !window.graphData[projectId][type]) {
            return;
        }
        const data = window.graphData[projectId][type];

        const containerId = 'graphviz-' + type + '-' + projectId;
        const container = document.getElementById(containerId);
        if (!container) {
            return;
        }

        Viz.instance().then(viz => {
            const svg = viz.renderSVGElement(data);
            container.replaceChildren(svg);
            const svgElement = container.getElementsByTagName('svg')[0];

            // Scratchblocks rendering in graph
            if (svgElement && typeof scratchblocks !== 'undefined') {
                const graphNodes = svgElement.querySelectorAll('.node');
                graphNodes.forEach(function (gNode) {
                    const textNodes = gNode.querySelectorAll('text');
                    if (textNodes.length === 0) return;

                    const contentLines = [];
                    let totalX = 0;
                    let totalY = 0;
                    let count = 0;

                    textNodes.forEach(function (t) {
                        const text = t.textContent;
                        if (text && text.trim().length > 0) {
                            contentLines.push(text);
                        }
                        const tx = parseFloat(t.getAttribute('x'));
                        const ty = parseFloat(t.getAttribute('y'));
                        if (!isNaN(tx) && !isNaN(ty)) {
                            totalX += tx;
                            totalY += ty;
                            count++;
                        }
                    });

                    const content = contentLines.join('\n').trim();
                    if (!content || content === 'Entry' || content === 'Exit') {
                        return;
                    }

                    try {
                        const doc = scratchblocks.parse(content, {
                            languages: ['${model.language()}', 'en']
                        });
                        const view = scratchblocks.newView(doc, {
                            style: 'scratch3',
                            scale: 0.6
                        });
                        const renderedBlock = view.render();

                        const centerX = count > 0 ? totalX / count : 0;
                        const centerY = count > 0 ? totalY / count : 0;

                        const width = parseFloat(renderedBlock.getAttribute('width'));
                        const height = parseFloat(renderedBlock.getAttribute('height'));

                        if (!isNaN(width) && !isNaN(height)) {
                            const x = centerX - (width / 2);
                            const y = centerY - (height / 2);
                            renderedBlock.setAttribute('x', x);
                            renderedBlock.setAttribute('y', y);
                        }

                        gNode.appendChild(renderedBlock);
                        textNodes.forEach(function (t) {
                            t.style.display = 'none';
                        });
                    } catch (err) {
                        console.warn('Scratchblocks parsing failed', err);
                    }
                });
            }

            if (svgElement && typeof svgPanZoom !== 'undefined') {
                svgElement.style.width = '100%';
                svgElement.style.height = '100%';
                svgPanZoom(svgElement, {
                    zoomEnabled: true,
                    controlIconsEnabled: true,
                    fit: true,
                    center: true
                });
            } else if (svgElement) {
                // Fallback if svgPanZoom not loaded
                svgElement.style.width = '100%';
                svgElement.style.height = 'auto';
            }
        }).catch((e) => {
            console.error(e);
            container.innerHTML = '<div style="color:red; padding:20px;">Error rendering graph: ' + e + '</div>';
        });
    }

    function filterSection(category, projectId) {
        const sectionId = 'section-' + category + '-' + projectId;
        const section = document.getElementById(sectionId);
        if (!section) {
            return;
        }

        const finderSelect = document.getElementById('finder-select-' + category + '-' + projectId);
        const spriteSelect = document.getElementById('sprite-select-' + category + '-' + projectId);

        const selectedFinder = finderSelect ? finderSelect.value : 'all';
        const selectedSprite = spriteSelect ? spriteSelect.value : 'all';

        const issues = section.querySelectorAll('.issue');

        issues.forEach(issue => {
            const finder = issue.getAttribute('data-finder');
            const sprite = issue.getAttribute('data-sprite');

            let visible = true;

            // Finder filter
            if (selectedFinder !== 'all' && finder !== selectedFinder) {
                visible = false;
            }

            // Sprite filter
            if (selectedSprite !== 'all' && sprite !== selectedSprite) {
                visible = false;
            }

            if (visible) {
                issue.classList.remove('hidden');
            } else {
                issue.classList.add('hidden');
            }
        });

        renderScratchBlocks();
    }

    const initialSelected = document.querySelector('.project-nav-item.active');
    selectProject(initialSelected.id.replace('nav-item-', ''), initialSelected.textContent);
</script>
</body>
</html>
