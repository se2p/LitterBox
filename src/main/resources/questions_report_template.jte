@import de.uni_passau.fim.se2.litterbox.report.HtmlJteQuestionResultHandler.QuestionsTemplateModel
@import de.uni_passau.fim.se2.litterbox.report.HtmlJteQuestionResultHandler.SpriteData
@import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension
@import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus
@import de.uni_passau.fim.se2.litterbox.analytics.questions.QuestionType
@import gg.jte.support.ForSupport

@param QuestionsTemplateModel model

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
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${model.translator().getInfo("questions_title")} ${model.projects().isEmpty() ? "LitterBox" : model.projects().getFirst().name()}</title>
    <link rel="icon" href="${model.favicon()}">
    <script>$unsafe{model.scripts().scratchBlocks()}</script>
    <script>$unsafe{model.scripts().scratchBlocksTranslations()}</script>
    <style>
        :root {
            --color-text: #4d97ff;
            --color-execution: #ff8c1a;
            --color-purpose: #40bf4a;
            --color-text-light: #4d97ff22;
            --color-execution-light: #ff8c1a22;
            --color-purpose-light: #40bf4a22;
            --color-correct: #22c55e;
            --color-incorrect: #ef4444;
            --color-partial: #f59e0b;
            --color-unanswered: #9ca3af;
        }

        * {
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: #f4f4f4;
            color: #333;
            margin: 0;
            padding: 20px;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
        }

        .boxed {
            background: #fff;
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 1px 5px rgba(0, 0, 0, 0.08);
            margin-bottom: 1.5rem;
        }

        .report-header {
            text-align: center;
            margin-bottom: 2rem;
        }

        .logo {
            display: inline-block;
            vertical-align: middle;
            margin-right: 1em;
        }

        .logo > svg {
            height: 4em;
            vertical-align: middle;
        }

        .report-header h1 {
            display: inline-block;
            vertical-align: middle;
            margin: 0;
            color: #333;
            font-size: 1.8em;
        }

        /* Layout */
        .with-sidebar {
            display: flex;
            flex-wrap: wrap;
            gap: 1.5rem;
        }

        .sidebar {
            flex-basis: 280px;
            flex-grow: 1;
        }

        .main-content {
            flex-basis: 0;
            flex-grow: 999;
            min-inline-size: 60%;
        }

        /* Block Model Matrix */
        .block-model-matrix {
            margin-bottom: 1rem;
        }

        .block-model-matrix h3 {
            margin-top: 0;
            margin-bottom: 0.75rem;
            font-size: 1em;
            color: #555;
        }

        .matrix-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.85em;
        }

        .matrix-table th,
        .matrix-table td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: center;
            cursor: pointer;
            transition: all 0.2s;
        }

        .matrix-table th {
            background: #f8f9fa;
            font-weight: 600;
            font-size: 0.9em;
        }

        .matrix-table td:hover {
            background: #e9ecef;
        }

        .matrix-table td.active {
            outline: 2px solid #007bff;
            outline-offset: -2px;
        }

        .matrix-table .row-TEXT { background-color: var(--color-text-light); }
        .matrix-table .row-EXECUTION { background-color: var(--color-execution-light); }
        .matrix-table .row-PURPOSE { background-color: var(--color-purpose-light); }

        .cell-count {
            font-weight: bold;
        }

        .cell-count-0 { color: #999; }
        .cell-count-1 { color: #666; }
        .cell-count-2 { color: #333; }
        .cell-count-3 { color: #000; }

        /* Filter sidebar */
        .filter-section {
            margin-bottom: 1.5rem;
        }

        .filter-section h4 {
            margin: 0 0 0.75rem 0;
            font-size: 0.95em;
            color: #555;
        }

        .filter-btn {
            display: block;
            width: 100%;
            padding: 8px 12px;
            margin-bottom: 6px;
            border: 1px solid #ddd;
            background: #fff;
            border-radius: 4px;
            cursor: pointer;
            text-align: left;
            font-size: 0.9em;
            transition: all 0.2s;
        }

        .filter-btn:hover {
            background: #f8f9fa;
        }

        .filter-btn.active, .filter-btn.scope-active {
            background: #007bff;
            color: white;
            border-color: #007bff;
        }

        .filter-btn .count {
            float: right;
            background: #e9ecef;
            padding: 2px 8px;
            border-radius: 10px;
            font-size: 0.85em;
        }

        .filter-btn.active .count, .filter-btn.scope-active .count {
            background: rgba(255,255,255,0.2);
        }

        /* Dimension colors */
        .dim-TEXT { border-left: 4px solid var(--color-text); }
        .dim-EXECUTION { border-left: 4px solid var(--color-execution); }
        .dim-PURPOSE { border-left: 4px solid var(--color-purpose); }

        /* Questions */
        .question-card {
            background: #fff;
            padding: 1.25rem;
            margin-bottom: 1rem;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
        }

        .question-card.dim-TEXT { border-left: 4px solid var(--color-text); }
        .question-card.dim-EXECUTION { border-left: 4px solid var(--color-execution); }
        .question-card.dim-PURPOSE { border-left: 4px solid var(--color-purpose); }

        .question-header {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 8px;
            margin-bottom: 0.75rem;
        }

        .question-number {
            font-weight: bold;
            color: #555;
        }

        .question-name {
            font-style: italic;
            color: #777;
            font-size: 0.9em;
        }

        .block-model-tag {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 4px;
            font-size: 0.75rem;
            font-weight: 600;
        }

        .tag-TEXT { background: var(--color-text-light); color: var(--color-text); }
        .tag-EXECUTION { background: var(--color-execution-light); color: var(--color-execution); }
        .tag-PURPOSE { background: var(--color-purpose-light); color: var(--color-purpose); }

        .question-type-tag {
            display: inline-block;
            padding: 3px 10px;
            border-radius: 4px;
            font-size: 0.75rem;
            background: #e9ecef;
            color: #555;
        }

        .sprite-name {
            color: #666;
            font-size: 0.9em;
        }

        .question-text {
            font-size: 1.05em;
            margin-bottom: 1rem;
            line-height: 1.5;
        }

        pre.blocks {
            background: #f8f9fa;
            border: 1px solid #eee;
            padding: 12px;
            border-radius: 6px;
            overflow-x: auto;
            margin: 1rem 0;
        }

        code.scratchblocks {
            font-family: sans-serif;
            font-weight: bold;
            padding: 2px 4px;
            border-radius: 4px;
        }

        /* Answer options */
        .answer-options {
            margin-top: 1rem;
        }

        .answer-option {
            display: flex;
            align-items: center;
            padding: 10px 14px;
            margin-bottom: 8px;
            background: #f8f9fa;
            border: 2px solid #e0e0e0;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;
        }

        .answer-option:hover {
            background: #f0f0f0;
            border-color: #ccc;
        }

        .answer-option.selected {
            background: #e3f2fd;
            border-color: #2196f3;
        }

        .answer-option.correct {
            background: #e8f5e9;
            border-color: #4caf50;
        }

        .answer-option.incorrect {
            background: #ffebee;
            border-color: #f44336;
        }

        .answer-option .option-letter {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: #e0e0e0;
            color: #555;
            font-weight: 600;
            margin-right: 12px;
            flex-shrink: 0;
        }

        .answer-option.selected .option-letter {
            background: #2196f3;
            color: white;
        }

        .answer-option.correct .option-letter {
            background: #4caf50;
            color: white;
        }

        .answer-option.incorrect .option-letter {
            background: #f44336;
            color: white;
        }

        .answer-option .option-text {
            flex: 1;
        }

        .answer-option .result-icon {
            margin-left: 10px;
            font-size: 1.2em;
        }

        /* Multi-select questions */
        .multiselect-hint {
            color: #666;
            font-size: 0.9em;
            margin-bottom: 0.5rem;
            padding: 6px 10px;
            background: #fff3cd;
            border-radius: 4px;
            border-left: 3px solid #ffc107;
        }

        .answer-options.multiselect .answer-option .option-letter {
            border-radius: 4px;
        }

        /* Free text input */
        .free-text-input {
            width: 100%;
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 6px;
            font-size: 1em;
            margin-top: 0.5rem;
            transition: border-color 0.2s;
        }

        .free-text-input:focus {
            outline: none;
            border-color: #2196f3;
        }

        .free-text-input.correct {
            border-color: #4caf50;
            background: #e8f5e9;
        }

        .free-text-input.incorrect {
            border-color: #f44336;
            background: #ffebee;
        }

        /* Buttons */
        .check-btn {
            display: inline-block;
            padding: 10px 20px;
            margin-top: 1rem;
            background: #007bff;
            color: white;
            border: none;
            border-radius: 6px;
            font-size: 0.95em;
            cursor: pointer;
            transition: background 0.2s;
        }

        .check-btn:hover {
            background: #0056b3;
        }

        .check-btn:disabled {
            background: #ccc;
            cursor: not-allowed;
        }

        /* Feedback */
        .feedback {
            margin-top: 1rem;
            padding: 12px 16px;
            border-radius: 6px;
            display: none;
        }

        .feedback.show {
            display: block;
        }

        .feedback.correct {
            background: #e8f5e9;
            color: #2e7d32;
            border: 1px solid #a5d6a7;
        }

        .feedback.incorrect {
            background: #ffebee;
            color: #c62828;
            border: 1px solid #ef9a9a;
        }

        .feedback.purpose-note {
            background: #fff3e0;
            color: #e65100;
            border: 1px solid #ffcc80;
            display: block;
        }

        /* Show answer toggle */
        .show-answer-btn {
            background: none;
            border: none;
            color: #007bff;
            cursor: pointer;
            font-size: 0.9em;
            padding: 0;
            margin-top: 0.5rem;
        }

        .show-answer-btn:hover {
            text-decoration: underline;
        }

        .correct-answer {
            margin-top: 0.5rem;
            padding: 10px;
            background: #e8f5e9;
            border-radius: 4px;
            display: none;
        }

        .correct-answer.show {
            display: block;
        }

        /* Stats bar */
        .stats-bar {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            padding: 1rem;
            background: #f8f9fa;
            border-radius: 8px;
            margin-bottom: 1rem;
        }

        .stat-item {
            text-align: center;
        }

        .stat-value {
            font-size: 1.5em;
            font-weight: bold;
            color: #007bff;
        }

        .stat-label {
            font-size: 0.8em;
            color: #666;
            text-transform: uppercase;
        }

        /* Score display */
        .score-display {
            position: sticky;
            top: 20px;
            padding: 1rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 8px;
            text-align: center;
            margin-bottom: 1rem;
        }

        .score-display h3 {
            margin: 0 0 0.5rem 0;
            font-size: 1em;
        }

        .score-value {
            font-size: 2em;
            font-weight: bold;
        }

        .score-detail {
            font-size: 0.85em;
            opacity: 0.9;
        }

        /* Project nav */
        .project-nav {
            margin-bottom: 1rem;
        }

        .project-nav-item {
            display: block;
            padding: 10px 14px;
            margin-bottom: 6px;
            background: #f8f9fa;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;
        }

        .project-nav-item:hover {
            background: #e9ecef;
        }

        .project-nav-item.active {
            background: #007bff;
            color: white;
        }

        /* Hidden class */
        .hidden {
            display: none !important;
        }

        /* Responsive */
        @@media (max-width: 900px) {
            .with-sidebar {
                flex-direction: column;
            }

            .sidebar {
                flex-basis: auto;
            }

            .main-content {
                min-inline-size: auto;
            }
        }

        footer {
            margin-top: 3rem;
            padding-top: 1rem;
            border-top: 1px solid #ddd;
            color: #777;
            font-size: 0.85em;
            text-align: center;
        }

        /* Teacher mode highlighting */
        .teacher-mode .answer-option.is-correct {
            background: #e8f5e9;
            border-color: #4caf50;
        }
        .teacher-mode .answer-option.is-correct .option-letter {
            background: #4caf50;
            color: white;
        }
        .teacher-mode .answer-option.is-correct::after {
            content: "✓ ${model.translator().getInfo("filter_correct")}";
            margin-left: auto;
            color: #4caf50;
            font-weight: 600;
            font-size: 0.85em;
        }

        /* Export mode styling */
        .export-mode .check-btn {
            display: none;
        }

        .export-mode .show-answer-btn {
            display: none;
        }

        .export-panel {
            background: #fffbeb;
            border: 1px solid #fcd34d;
        }

        .export-btn {
            width: 100%;
            padding: 12px 16px;
            background: #2563eb;
            color: white;
            border: none;
            border-radius: 6px;
            font-size: 1em;
            font-weight: 500;
            cursor: pointer;
            transition: background 0.2s;
        }

        .export-btn:hover {
            background: #1d4ed8;
        }

        .export-btn:disabled {
            background: #9ca3af;
            cursor: not-allowed;
        }

        /* Sprite filter - matching filter button style */
        .sprite-filter-section {
            margin-bottom: 1rem;
        }
        .sprite-filter-section h4 {
            margin: 0 0 0.75rem 0;
            font-size: 0.95em;
            color: #555;
        }
        .sprite-select {
            width: 100%;
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 0.9em;
            background: #fff;
            cursor: pointer;
            appearance: none;
            -webkit-appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23555' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 10px center;
            padding-right: 30px;
        }
        .sprite-select:hover {
            background-color: #f8f9fa;
            border-color: #ccc;
        }
        .sprite-select:focus {
            outline: none;
            border-color: #007bff;
            box-shadow: 0 0 0 2px rgba(0,123,255,0.1);
        }

        /* Code viewer */
        .code-viewer-btn {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            background: #f0f0f0;
            border: 1px solid #ddd;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.85em;
            color: #555;
            margin-left: auto;
        }
        .code-viewer-btn:hover {
            background: #e0e0e0;
            border-color: #bbb;
        }
        .sprite-code-panel {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 90%;
            max-width: 900px;
            max-height: 80vh;
            padding: 1.5rem;
            background: #fff;
            border: 1px solid #ddd;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            z-index: 1000;
            display: none;
            overflow: hidden;
        }
        .sprite-code-panel.show {
            display: flex;
            flex-direction: column;
        }
        .sprite-code-overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.4);
            z-index: 999;
            display: none;
        }
        .sprite-code-overlay.show {
            display: block;
        }
        .sprite-code-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
            padding-bottom: 0.75rem;
            border-bottom: 1px solid #eee;
        }
        .sprite-code-header h4 {
            margin: 0;
            color: #333;
            font-size: 1.1em;
        }
        .close-code-btn {
            background: #f0f0f0;
            border: none;
            font-size: 1.5em;
            cursor: pointer;
            color: #666;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s;
        }
        .close-code-btn:hover {
            background: #e0e0e0;
            color: #333;
        }
        .sprite-code-tabs {
            display: flex;
            flex-wrap: wrap;
            gap: 6px;
            margin-bottom: 1rem;
            padding-bottom: 0.75rem;
            border-bottom: 1px solid #eee;
        }
        .sprite-tab {
            padding: 8px 16px;
            background: #e9ecef;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 0.9em;
            transition: all 0.2s;
        }
        .sprite-tab:hover {
            background: #dee2e6;
        }
        .sprite-tab.active {
            background: #007bff;
            color: white;
        }
        .sprite-code-content {
            flex: 1;
            overflow-y: auto;
            max-height: calc(80vh - 150px);
        }
        .sprite-local-data {
            background: #f0f7ff;
            border: 1px solid #cce5ff;
            border-radius: 6px;
            padding: 10px 14px;
            margin-bottom: 1rem;
        }
        .sprite-local-data-label {
            font-size: 0.8em;
            color: #666;
            margin-bottom: 6px;
            font-weight: 600;
        }
        .sprite-local-data-items {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            align-items: center;
        }
        .sprite-local-data-items code.scratchblocks {
            display: inline-block;
        }
        .data-type-label {
            font-size: 0.75em;
            color: #888;
            margin-right: 4px;
        }
        .no-code-message {
            padding: 20px;
            text-align: center;
            color: #666;
            font-style: italic;
            background: #f8f9fa;
            border-radius: 4px;
        }
    </style>
</head>
<body>
<div class="container ${model.teacherMode() ? "teacher-mode" : ""} ${model.exportMode() ? "export-mode" : ""}">
    <header class="report-header boxed">
        <div class="logo">
            $unsafe{model.logo()}
        </div>
        <h1>${model.translator().getInfo("questions_title")} ${model.projects().isEmpty() ? "Scratch Project" : model.projects().getFirst().name()}</h1>
        @if(model.teacherMode())
            <div style="margin-top: 0.5rem; color: #666; font-size: 0.9em;">
                📚 ${model.translator().getInfo("questions_teacher_mode")}</div>
        @elseif(model.exportMode())
            <div style="margin-top: 0.5rem; color: #666; font-size: 0.9em;">
                📝 ${model.translator().getInfo("questions_export_mode")}</div>
        @endif
    </header>

    <div class="with-sidebar">
        <div class="sidebar">
            @if(model.projects().size() > 1)
            <div class="boxed project-nav">
                <h3 style="margin-top:0">${model.translator().getInfo("questions_projects")}</h3>
                @for(var project : ForSupport.of(model.projects()))
                    <div class="project-nav-item ${project.isFirst() ? "active" : ""}"
                         id="nav-${project.get().id()}"
                         onclick="selectProject('${project.get().id()}')">
                        ${project.get().name()}
                    </div>
                @endfor
            </div>
            @endif

            @if(model.exportMode())
                <div class="boxed export-panel">
                    <h3>${model.translator().getInfo("questions_export_answers")}</h3>
                    <p style="font-size: 0.9em; color: #666; margin-bottom: 1rem;">
                        ${model.translator().getInfo("questions_export_instructions")}
                    </p>
                    <button class="export-btn" onclick="exportAnswers()">
                        📥 ${model.translator().getInfo("questions_export_button")}
                    </button>
                    <div id="export-status" style="margin-top: 0.5rem; font-size: 0.85em;"></div>
                </div>
            @elseif(!model.teacherMode())
            <div class="boxed score-display">
                <h3>${model.translator().getInfo("questions_your_score")}</h3>
                <div class="score-value"><span id="score-correct">0</span>/<span id="score-total">0</span></div>
                <div class="score-detail" id="score-percentage">0%</div>
            </div>
            @endif

            <div class="boxed">
                <div class="block-model-matrix">
                    <h3>${model.translator().getInfo("questions_block_model")}</h3>
                    <table class="matrix-table" id="block-model-table">
                        <tr>
                            <th></th>
                            @for(ScopeOfFocus scope : ScopeOfFocus.values())
                                <th>${scope.name().substring(0, 1) + scope.name().substring(1).toLowerCase()}</th>
                            @endfor
                        </tr>
                        @for(ProgramDimension dim : ProgramDimension.values())
                            <tr>
                                <th class="row-${dim.name()}">${dim.name().substring(0, 1) + dim.name().substring(1).toLowerCase()}</th>
                                @for(ScopeOfFocus scope : ScopeOfFocus.values())
                                    <td class="row-${dim.name()}"
                                        data-cell="${dim.name()}/${scope.name()}"
                                        onclick="filterByCell('${dim.name()}/${scope.name()}')">
                                        <span class="cell-count" id="cell-${dim.name()}-${scope.name()}">0</span>
                                    </td>
                                @endfor
                            </tr>
                        @endfor
                    </table>
                </div>

                <div class="filter-section">
                    <h4>${model.translator().getInfo("questions_filter_dimension")}</h4>
                    <button class="filter-btn active" data-filter="all" onclick="filterByDimension('all')">
                        ${model.translator().getInfo("filter_all_finders")} <span class="count" id="count-all">0</span>
                    </button>
                    <button class="filter-btn dim-TEXT" data-filter="TEXT" onclick="filterByDimension('TEXT')">
                        ${model.translator().getInfo("questions_text_syntax")} <span class="count" id="count-TEXT">0</span>
                    </button>
                    <button class="filter-btn dim-EXECUTION" data-filter="EXECUTION" onclick="filterByDimension('EXECUTION')">
                        ${model.translator().getInfo("questions_execution_tracing")} <span class="count" id="count-EXECUTION">0</span>
                    </button>
                    <button class="filter-btn dim-PURPOSE" data-filter="PURPOSE" onclick="filterByDimension('PURPOSE')">
                        ${model.translator().getInfo("questions_purpose_intent")} <span class="count" id="count-PURPOSE">0</span>
                    </button>
                </div>

                <div class="filter-section">
                    <h4>${model.translator().getInfo("questions_filter_scope")}</h4>
                    <button class="filter-btn scope-active" data-scope="all" onclick="filterByScope('all')">
                        ${model.translator().getInfo("filter_all_finders")} <span class="count" id="count-scope-all">0</span>
                    </button>
                    <button class="filter-btn" data-scope="ATOM" onclick="filterByScope('ATOM')">
                        ${model.translator().getInfo("questions_scope_atom")} <span class="count" id="count-ATOM">0</span>
                    </button>
                    <button class="filter-btn" data-scope="BLOCK" onclick="filterByScope('BLOCK')">
                        ${model.translator().getInfo("questions_scope_block")} <span class="count" id="count-BLOCK">0</span>
                    </button>
                    <button class="filter-btn" data-scope="RELATION" onclick="filterByScope('RELATION')">
                        ${model.translator().getInfo("questions_scope_relation")} <span class="count" id="count-RELATION">0</span>
                    </button>
                    <button class="filter-btn" data-scope="MACRO" onclick="filterByScope('MACRO')">
                        ${model.translator().getInfo("questions_scope_macro")} <span class="count" id="count-MACRO">0</span>
                    </button>
                </div>

                <div class="filter-section">
                    <h4>${model.translator().getInfo("questions_filter_type")}</h4>
                    <button class="filter-btn" data-filter-type="MULTIPLE_CHOICE" onclick="filterByType('MULTIPLE_CHOICE')">
                        ${model.translator().getInfo("questions_multiple_choice")} <span class="count" id="count-mc">0</span>
                    </button>
                    <button class="filter-btn" data-filter-type="NUMBER" onclick="filterByType('NUMBER')">
                        ${model.translator().getInfo("questions_number")} <span class="count" id="count-number">0</span>
                    </button>
                    <button class="filter-btn" data-filter-type="YES_NO" onclick="filterByType('YES_NO')">
                        ${model.translator().getInfo("questions_yes_no")} <span class="count" id="count-yesno">0</span>
                    </button>
                    <button class="filter-btn" data-filter-type="FREE_TEXT" onclick="filterByType('FREE_TEXT')">
                        ${model.translator().getInfo("questions_free_text")} <span class="count" id="count-freetext">0</span>
                    </button>
                </div>

                <div class="filter-section sprite-filter-section">
                    <h4>${model.translator().getInfo("filter_by_sprite")}</h4>
                    <select class="sprite-select" id="sprite-filter" onchange="filterBySprite(this.value)">
                        <option value="all">${model.translator().getInfo("filter_all_sprites")}</option>
                        @for(var project : model.projects())
                            @for(var sprite : project.sprites())
                                <option value="${sprite}">${sprite}</option>
                            @endfor
                        @endfor
                    </select>
                </div>
            </div>
        </div>

        <div class="main-content">
            @for(var project : ForSupport.of(model.projects()))
                <div class="project-content ${project.isFirst() ? "" : "hidden"}" id="content-${project.get().id()}">
                    <div class="stats-bar">
                        <div class="stat-item">
                            <div class="stat-value">${project.get().questions().size()}</div>
                            <div class="stat-label">${model.translator().getInfo("questions_questions")}</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">${project.get().questions().stream().filter(q -> q.questionType() == QuestionType.MULTIPLE_CHOICE).count()}</div>
                            <div class="stat-label">${model.translator().getInfo("questions_multiple_choice")}</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">${project.get().questions().stream().filter(q -> q.questionType() == QuestionType.NUMBER).count()}</div>
                            <div class="stat-label">${model.translator().getInfo("questions_number")}</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">${project.get().questions().stream().filter(q -> q.questionType() == QuestionType.FREE_TEXT).count()}</div>
                            <div class="stat-label">${model.translator().getInfo("questions_free_text")}</div>
                        </div>
                    </div>

                    <div class="questions-container" id="questions-${project.get().id()}">
                        @for(var question : ForSupport.of(project.get().questions()))
                            !{int qNum = question.getIndex() + 1;}
                            <div class="question-card dim-${question.get().programDimension().name()}"
                                 id="question-${project.get().id()}-${question.get().id()}"
                                 data-question-id="${question.get().id()}"
                                 data-finder="${question.get().finder()}"
                                 data-dimension="${question.get().programDimension().name()}"
                                 data-scope="${question.get().scopeOfFocus().name()}"
                                 data-cell="${question.get().blockModelCell()}"
                                 data-type="${question.get().questionType().name()}"
                                 data-sprite="${question.get().sprite()}"
                                 data-answers="${String.join("|", question.get().normalizedAnswers())}"
                                 data-translatable-answers='${question.get().translatableAnswersJson()}'
                                 data-multiselect="${question.get().isMultiSelect()}"
                                 data-expected-count="${question.get().correctAnswers() != null ? question.get().correctAnswers().size() : 0}">

                                <div class="question-header">
                                    <span class="question-number">Q${qNum}</span>
                                    <span class="question-name">${question.get().translatedName()}</span>
                                    <span class="block-model-tag tag-${question.get().programDimension().name()}">
                                        ${question.get().programDimension().name()}/${question.get().scopeOfFocus().name()}
                                    </span>
                                    <span class="question-type-tag">${question.get().questionType().name().replace("_", " ")}</span>
                                    <span class="sprite-name">${model.translator().getInfo("sprite")}: ${question.get().sprite()}</span>
                                    <button class="code-viewer-btn" onclick="showSpriteCode('${project.get().id()}', '${question.get().sprite()}')">
                                        📝 ${model.translator().getInfo("questions_view_code")}
                                    </button>
                                </div>

                                <div class="question-text">$unsafe{question.get().questionText()}</div>

                                @if(question.get().scratchBlocks() != null && !question.get().scratchBlocks().isEmpty())
                                    <pre class="blocks">${question.get().scratchBlocks()}</pre>
                                @endif

                                @if(question.get().questionType() == QuestionType.MULTIPLE_CHOICE && question.get().displayChoices() != null)
                                    @if(question.get().isMultiSelect() && !model.teacherMode())
                                        <div class="multiselect-hint">
                                            &#9745; ${model.translator().getInfo("questions_select_multiple")}
                                        </div>
                                    @endif
                                    <div class="answer-options ${question.get().isMultiSelect() ? "multiselect" : ""}"
                                         id="options-${project.get().id()}-${question.get().id()}">
                                        @for(var choice : ForSupport.of(question.get().displayChoices()))
                                            !{char letter = (char)('A' + choice.getIndex());}
                                            !{String normalizedChoice = question.get().normalizedChoices().get(choice.getIndex());}
                                            !{boolean isCorrect = question.get().normalizedAnswers().contains(normalizedChoice);}
                                            @if(model.teacherMode())
                                            <div class="answer-option ${isCorrect ? "is-correct" : ""}"
                                                 data-value="${normalizedChoice}">
                                                <span class="option-letter">${letter}</span>
                                                <span class="option-text">$unsafe{choice.get()}</span>
                                                <span class="result-icon"></span>
                                            </div>
                                            @else
                                            <div class="answer-option"
                                                 data-value="${normalizedChoice}"
                                                 onclick="selectOption(this, '${project.get().id()}', ${question.get().id()})">
                                                <span class="option-letter">${letter}</span>
                                                <span class="option-text">$unsafe{choice.get()}</span>
                                                <span class="result-icon"></span>
                                            </div>
                                            @endif
                                        @endfor
                                    </div>
                                    @if(!model.teacherMode() && question.get().canCheck())
                                        <button class="check-btn" onclick="checkAnswer('${project.get().id()}', ${question.get().id()}, 'mc')">
                                            ${model.translator().getInfo("questions_check_answer")}
                                        </button>
                                    @elseif(!model.teacherMode() && !question.get().canCheck())
                                        <div style="display: flex; gap: 10px; margin-top: 1rem;">
                                            <button class="check-btn"
                                                    style="background: var(--color-correct); margin-top: 0;"
                                                    onclick="markManual('${project.get().id()}', ${question.get().id()}, true)">
                                                ${model.translator().getInfo("filter_correct")}
                                            </button>
                                            <button class="check-btn"
                                                    style="background: var(--color-incorrect); margin-top: 0;"
                                                    onclick="markManual('${project.get().id()}', ${question.get().id()}, false)">
                                                ${model.translator().getInfo("filter_incorrect")}
                                            </button>
                                        </div>
                                        <div class="feedback purpose-note">
                                            &#128161; ${model.translator().getInfo("questions_teacher_verify")}
                                        </div>
                                    @endif
                                @elseif(question.get().questionType() == QuestionType.NUMBER)
                                    @if(model.teacherMode() && question.get().correctAnswers() != null && !question.get().correctAnswers().isEmpty())
                                        <div class="correct-answer show">
                                            <strong>${model.translator().getInfo("questions_correct_answer")}</strong> ${String.join(", ", question.get().correctAnswers())}
                                        </div>
                                    @else
                                        <input type="number" class="free-text-input"
                                               id="input-${project.get().id()}-${question.get().id()}"
                                               placeholder="${model.translator().getInfo("questions_enter_number")}">
                                        @if(question.get().canCheck())
                                            <button class="check-btn" onclick="checkAnswer('${project.get().id()}', ${question.get().id()}, 'number')">
                                                ${model.translator().getInfo("questions_check_answer")}
                                            </button>
                                        @else
                                            <div style="display: flex; gap: 10px; margin-top: 1rem;">
                                                <button class="check-btn"
                                                        style="background: var(--color-correct); margin-top: 0;"
                                                        onclick="markManual('${project.get().id()}', ${question.get().id()}, true)">
                                                    ${model.translator().getInfo("filter_correct")}
                                                </button>
                                                <button class="check-btn"
                                                        style="background: var(--color-incorrect); margin-top: 0;"
                                                        onclick="markManual('${project.get().id()}', ${question.get().id()}, false)">
                                                    ${model.translator().getInfo("filter_incorrect")}
                                                </button>
                                            </div>
                                            <div class="feedback purpose-note">
                                                &#128161; ${model.translator().getInfo("questions_teacher_verify")}
                                            </div>
                                        @endif
                                    @endif
                                @elseif(question.get().questionType() == QuestionType.YES_NO)
                                    !{boolean yesCorrect = question.get().normalizedAnswers().contains(model.translator().getInfo("yes_term").toLowerCase());}
                                    !{boolean noCorrect = question.get().normalizedAnswers().contains(model.translator().getInfo("no_term").toLowerCase());}
                                    <div class="answer-options" id="options-${project.get().id()}-${question.get().id()}">
                                        @if(model.teacherMode())
                                        <div class="answer-option ${yesCorrect ? "is-correct" : ""}"
                                             data-value="${model.translator().getInfo("yes_term").toLowerCase()}">
                                            <span class="option-letter">Y</span>
                                            <span class="option-text">${model.translator().getInfo("yes")}</span>
                                            <span class="result-icon"></span>
                                        </div>
                                        <div class="answer-option ${noCorrect ? "is-correct" : ""}"
                                             data-value="${model.translator().getInfo("no_term").toLowerCase()}">
                                            <span class="option-letter">N</span>
                                            <span class="option-text">${model.translator().getInfo("no")}</span>
                                            <span class="result-icon"></span>
                                        </div>
                                        @else
                                        <div class="answer-option"
                                             data-value="${model.translator().getInfo("yes_term").toLowerCase()}"
                                             onclick="selectOption(this, '${project.get().id()}', ${question.get().id()})">
                                            <span class="option-letter">Y</span>
                                            <span class="option-text">${model.translator().getInfo("yes")}</span>
                                            <span class="result-icon"></span>
                                        </div>
                                        <div class="answer-option"
                                             data-value="${model.translator().getInfo("no_term").toLowerCase()}"
                                             onclick="selectOption(this, '${project.get().id()}', ${question.get().id()})">
                                            <span class="option-letter">N</span>
                                            <span class="option-text">${model.translator().getInfo("no")}</span>
                                            <span class="result-icon"></span>
                                        </div>
                                        @endif
                                    </div>
                                    @if(!model.teacherMode() && question.get().canCheck())
                                        <button class="check-btn" onclick="checkAnswer('${project.get().id()}', ${question.get().id()}, 'yesno')">
                                            ${model.translator().getInfo("questions_check_answer")}
                                        </button>
                                    @elseif(!model.teacherMode() && !question.get().canCheck())
                                        <div style="display: flex; gap: 10px; margin-top: 1rem;">
                                            <button class="check-btn"
                                                    style="background: var(--color-correct); margin-top: 0;"
                                                    onclick="markManual('${project.get().id()}', ${question.get().id()}, true)">
                                                ${model.translator().getInfo("filter_correct")}
                                            </button>
                                            <button class="check-btn"
                                                    style="background: var(--color-incorrect); margin-top: 0;"
                                                    onclick="markManual('${project.get().id()}', ${question.get().id()}, false)">
                                                ${model.translator().getInfo("filter_incorrect")}
                                            </button>
                                        </div>
                                        <div class="feedback purpose-note">
                                            &#128161; ${model.translator().getInfo("questions_teacher_verify")}
                                        </div>
                                    @endif
                                @elseif(question.get().questionType() == QuestionType.FREE_TEXT)
                                    @if(model.teacherMode() && question.get().correctAnswers() != null && !question.get().correctAnswers().isEmpty())
                                        <div class="correct-answer show">
                                            <strong>${model.translator().getInfo("questions_expected_answer")}</strong> ${String.join(", ", question.get().correctAnswers())}
                                        </div>
                                    @else
                                        <textarea class="free-text-input" rows="3"
                                                  id="input-${project.get().id()}-${question.get().id()}"
                                                  placeholder="${model.translator().getInfo("questions_enter_answer")}"></textarea>
                                        @if(question.get().canCheck())
                                            <button class="check-btn" onclick="checkAnswer('${project.get().id()}', ${question.get().id()}, 'freetext')">
                                                ${model.translator().getInfo("questions_check_answer")}
                                            </button>
                                        @elseif(!model.exportMode())
                                            <div style="display: flex; gap: 10px; margin-top: 1rem;">
                                                <button class="check-btn"
                                                        style="background: var(--color-correct); margin-top: 0;"
                                                        onclick="markManual('${project.get().id()}', ${question.get().id()}, true)">
                                                    ${model.translator().getInfo("filter_correct")}
                                                </button>
                                                <button class="check-btn"
                                                        style="background: var(--color-incorrect); margin-top: 0;"
                                                        onclick="markManual('${project.get().id()}', ${question.get().id()}, false)">
                                                    ${model.translator().getInfo("filter_incorrect")}
                                                </button>
                                            </div>
                                            <div class="feedback purpose-note">
                                                &#128161; ${model.translator().getInfo("questions_freetext_note")}
                                            </div>
                                        @endif
                                    @endif
                                @endif

                                <div class="feedback" id="feedback-${project.get().id()}-${question.get().id()}"></div>

                                @if(model.showAnswers() && !model.teacherMode() && question.get().correctAnswers() != null && !question.get().correctAnswers().isEmpty())
                                    <button class="show-answer-btn" onclick="toggleAnswer('${project.get().id()}', ${question.get().id()})">
                                        ${model.translator().getInfo("questions_correct_answer")}
                                    </button>
                                    <div class="correct-answer" id="answer-${project.get().id()}-${question.get().id()}">
                                        <strong>${model.translator().getInfo("questions_correct_answer")}</strong> $unsafe{question.get().formattedCorrectAnswers()}
                                    </div>
                                @endif
                            </div>
                        @endfor
                    </div>

                    <%-- Sprite code panel overlay and modal for viewing sprite scripts --%>
                    <div class="sprite-code-overlay" id="code-overlay-${project.get().id()}" onclick="hideCodePanel('${project.get().id()}')"></div>
                    <div class="sprite-code-panel" id="code-panel-${project.get().id()}">
                        <div class="sprite-code-header">
                            <h4>${model.translator().getInfo("questions_sprite_code")} <span id="code-sprite-name-${project.get().id()}"></span></h4>
                            <button class="close-code-btn" onclick="hideCodePanel('${project.get().id()}')">&times;</button>
                        </div>
                        <div class="sprite-code-tabs" id="code-tabs-${project.get().id()}">
                            @for(var sprite : project.get().spriteCode().keySet())
                                <button class="sprite-tab" data-sprite="${sprite}" onclick="showSpriteTab('${project.get().id()}', '${sprite}')">
                                    ${sprite}
                                </button>
                            @endfor
                        </div>
                        <div class="sprite-code-content">
                            @for(var entry : project.get().spriteCode().entrySet())
                                !{String spriteName = entry.getKey();}
                                !{SpriteData data = project.get().spriteData().get(spriteName);}
                                <div class="sprite-code-view hidden" id="code-${project.get().id()}-${spriteName.replaceAll("[^a-zA-Z0-9]", "-")}">
                                    @if(data != null && (!data.variables().isEmpty() || !data.lists().isEmpty()))
                                    <div class="sprite-local-data">
                                        <div class="sprite-local-data-label">${model.translator().getInfo("questions_local_vars")}</div>
                                        <div class="sprite-local-data-items">
                                            @for(var varName : data.variables())
                                                <code class="scratchblocks">(${varName} :: variables)</code>
                                            @endfor
                                            @for(var listName : data.lists())
                                                <code class="scratchblocks">(${listName} :: list)</code>
                                            @endfor
                                        </div>
                                    </div>
                                    @endif
                                    @if(!entry.getValue().isEmpty())
                                    <pre class="blocks sprite-code-blocks">${entry.getValue()}</pre>
                                    @else
                                    <div class="no-code-message">${model.translator().getInfo("questions_no_scripts")}</div>
                                    @endif
                                </div>
                            @endfor
                        </div>
                    </div>
                </div>
            @endfor
        </div>
    </div>

    <footer>
        <p>${model.translator().getInfo("questions_generated_by")}</p>
        <p>${model.translator().getInfo("questions_scratchblocks_credit")} <a href="https://github.com/scratchblocks/scratchblocks">scratchblocks</a></p>
    </footer>
</div>

<script>
    // State
    let currentProject = '$unsafe{model.projects().isEmpty() ? "" : model.projects().get(0).id()}';
    let currentFilter = 'all';
    let currentTypeFilter = null;
    let currentCellFilter = null;
    let currentSpriteFilter = 'all';
    let currentScopeFilter = 'all';
    let answeredQuestions = {};
    let correctCount = 0;
    let totalCheckable = 0;

    // Translations
    const i18n = {
        feedbackCorrect: '✓ ${model.translator().getInfo("questions_feedback_correct")}',
        feedbackIncorrect: '✗ ${model.translator().getInfo("questions_feedback_incorrect")}'
    };

    // Helper function to render scratchblocks with translation
    const targetLang = '${model.language()}';

    function renderAndTranslate(selector, options = {}) {
        document.querySelectorAll(selector).forEach(el => {
            const code = el.textContent;
            if (!code.trim()) return;

            // Parse the code (generated in English by ScratchBlocksVisitor)
            const doc = scratchblocks.parse(code, {languages: ['en']});

            // Translate to target language if not English
            if (targetLang !== 'en' && scratchblocks.allLanguages[targetLang]) {
                doc.translate(scratchblocks.allLanguages[targetLang]);
            }

            // Render to SVG
            const view = scratchblocks.newView(doc, {
                style: options.style || 'scratch3',
                scale: options.scale || 0.7
            });

            // Replace content
            el.innerHTML = '';
            el.appendChild(view.render());
        });
    }

    function renderInlineAndTranslate(selector, options = {}) {
        document.querySelectorAll(selector).forEach(el => {
            const code = el.textContent;
            if (!code.trim()) return;

            // Parse the code
            const doc = scratchblocks.parse(code, {languages: ['en'], inline: true});

            // Translate to target language if not English
            if (targetLang !== 'en' && scratchblocks.allLanguages[targetLang]) {
                doc.translate(scratchblocks.allLanguages[targetLang]);
            }

            // Render to SVG
            const view = scratchblocks.newView(doc, {
                style: options.style || 'scratch3',
                scale: options.scale || 0.7
            });

            // Replace content
            el.innerHTML = '';
            el.appendChild(view.render());
        });
    }

    // Initialize
    document.addEventListener('DOMContentLoaded', function() {
        renderAndTranslate('pre.blocks:not(.sprite-code-blocks)', {
            style: 'scratch3',
            scale: 0.7
        });
        // Render inline scratchblocks in question text
        renderInlineAndTranslate('code.scratchblocks', {
            style: 'scratch3',
            scale: 0.7
        });
        updateCounts();
        updateScore();
    });

    // Project selection
    function selectProject(projectId) {
        document.querySelectorAll('.project-nav-item').forEach(el => el.classList.remove('active'));
        document.getElementById('nav-' + projectId).classList.add('active');

        document.querySelectorAll('.project-content').forEach(el => el.classList.add('hidden'));
        document.getElementById('content-' + projectId).classList.remove('hidden');

        currentProject = projectId;
        updateCounts();
        applyFilters();
    }

    // Filter by dimension
    function filterByDimension(dimension) {
        currentFilter = dimension;
        currentCellFilter = null;
        currentTypeFilter = null;

        document.querySelectorAll('.filter-btn[data-filter]').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.filter === dimension);
        });
        document.querySelectorAll('.filter-btn[data-filter-type]').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelectorAll('.matrix-table td').forEach(td => {
            td.classList.remove('active');
        });

        applyFilters();
    }

    // Filter by question type
    function filterByType(type) {
        if (currentTypeFilter === type) {
            currentTypeFilter = null;
            document.querySelectorAll('.filter-btn[data-filter-type]').forEach(btn => {
                btn.classList.remove('active');
            });
        } else {
            currentTypeFilter = type;
            document.querySelectorAll('.filter-btn[data-filter-type]').forEach(btn => {
                btn.classList.toggle('active', btn.dataset.filterType === type);
            });
        }

        applyFilters();
    }

    // Filter by block model cell
    function filterByCell(cell) {
        if (currentCellFilter === cell) {
            currentCellFilter = null;
            document.querySelectorAll('.matrix-table td').forEach(td => {
                td.classList.remove('active');
            });
        } else {
            currentCellFilter = cell;
            currentFilter = 'all';
            document.querySelectorAll('.filter-btn[data-filter]').forEach(btn => {
                btn.classList.toggle('active', btn.dataset.filter === 'all');
            });
            document.querySelectorAll('.matrix-table td').forEach(td => {
                td.classList.toggle('active', td.dataset.cell === cell);
            });
        }

        applyFilters();
    }

    // Filter by sprite
    function filterBySprite(sprite) {
        currentSpriteFilter = sprite;
        applyFilters();
    }

    // Filter by scope
    function filterByScope(scope) {
        if (scope === 'all') {
            currentScopeFilter = 'all';
        } else if (currentScopeFilter === scope) {
            currentScopeFilter = 'all';
        } else {
            currentScopeFilter = scope;
        }

        document.querySelectorAll('.filter-btn[data-scope]').forEach(btn => {
            btn.classList.toggle('scope-active', btn.dataset.scope === currentScopeFilter);
        });

        applyFilters();
    }

    // Apply all filters
    function applyFilters() {
        const container = document.getElementById('questions-' + currentProject);
        if (!container) return;

        container.querySelectorAll('.question-card').forEach(card => {
            let show = true;

            if (currentFilter !== 'all' && card.dataset.dimension !== currentFilter) {
                show = false;
            }

            if (currentCellFilter && card.dataset.cell !== currentCellFilter) {
                show = false;
            }

            if (currentTypeFilter && card.dataset.type !== currentTypeFilter) {
                show = false;
            }

            if (currentSpriteFilter !== 'all' && card.dataset.sprite !== currentSpriteFilter) {
                show = false;
            }

            if (currentScopeFilter !== 'all' && card.dataset.scope !== currentScopeFilter) {
                show = false;
            }

            card.classList.toggle('hidden', !show);
        });
    }

    // Update counts
    function updateCounts() {
        const container = document.getElementById('questions-' + currentProject);
        if (!container) return;

        const cards = container.querySelectorAll('.question-card');
        let counts = { all: 0, TEXT: 0, EXECUTION: 0, PURPOSE: 0 };
        let typeCounts = { MULTIPLE_CHOICE: 0, NUMBER: 0, YES_NO: 0, FREE_TEXT: 0 };
        let scopeCounts = { all: 0, ATOM: 0, BLOCK: 0, RELATION: 0, MACRO: 0 };
        let cellCounts = {};

        cards.forEach(card => {
            counts.all++;
            counts[card.dataset.dimension]++;
            typeCounts[card.dataset.type]++;
            scopeCounts.all++;
            if (card.dataset.scope) {
                scopeCounts[card.dataset.scope]++;
            }

            const cell = card.dataset.cell;
            cellCounts[cell] = (cellCounts[cell] || 0) + 1;
        });

        // Update dimension counts
        document.getElementById('count-all').textContent = counts.all;
        document.getElementById('count-TEXT').textContent = counts.TEXT;
        document.getElementById('count-EXECUTION').textContent = counts.EXECUTION;
        document.getElementById('count-PURPOSE').textContent = counts.PURPOSE;

        // Update scope counts
        document.getElementById('count-scope-all').textContent = scopeCounts.all;
        document.getElementById('count-ATOM').textContent = scopeCounts.ATOM;
        document.getElementById('count-BLOCK').textContent = scopeCounts.BLOCK;
        document.getElementById('count-RELATION').textContent = scopeCounts.RELATION;
        document.getElementById('count-MACRO').textContent = scopeCounts.MACRO;

        // Update type counts
        document.getElementById('count-mc').textContent = typeCounts.MULTIPLE_CHOICE;
        document.getElementById('count-number').textContent = typeCounts.NUMBER;
        document.getElementById('count-yesno').textContent = typeCounts.YES_NO;
        document.getElementById('count-freetext').textContent = typeCounts.FREE_TEXT;

        // Update matrix cells
        ['TEXT', 'EXECUTION', 'PURPOSE'].forEach(dim => {
            ['ATOM', 'BLOCK', 'RELATION', 'MACRO'].forEach(scope => {
                const cellId = 'cell-' + dim + '-' + scope;
                const count = cellCounts[dim + '/' + scope] || 0;
                const el = document.getElementById(cellId);
                if (el) {
                    el.textContent = count;
                    el.className = 'cell-count cell-count-' + Math.min(count, 3);
                }
            });
        });

        // Count all questions for total points
        totalCheckable = cards.length;
        updateScore();
    }

    // Select option (for MC and Yes/No)
    function selectOption(element, projectId, questionId) {
        const card = document.getElementById('question-' + projectId + '-' + questionId);
        const isMultiSelect = card.dataset.multiselect === 'true';
        const container = element.parentElement;

        if (isMultiSelect) {
            // Toggle selection for multi-select
            element.classList.toggle('selected');
        } else {
            // Single selection - deselect others first
            container.querySelectorAll('.answer-option').forEach(opt => {
                opt.classList.remove('selected');
            });
            element.classList.add('selected');
        }
    }

    // Check answer
    function checkAnswer(projectId, questionId, type) {
        const card = document.getElementById('question-' + projectId + '-' + questionId);
        const feedback = document.getElementById('feedback-' + projectId + '-' + questionId);
        const answers = card.dataset.answers.split('|').filter(a => a);
        const qKey = projectId + '-' + questionId;
        const isMultiSelect = card.dataset.multiselect === 'true';
        const expectedCount = parseInt(card.dataset.expectedCount) || 1;

        let userAnswer;
        let isCorrect = false;

        if (type === 'mc' || type === 'yesno') {
            const selectedOptions = card.querySelectorAll('.answer-option.selected');
            if (selectedOptions.length === 0) {
                feedback.textContent = 'Please select an answer first.';
                feedback.className = 'feedback show incorrect';
                return;
            }

            if (isMultiSelect) {
                // Multi-select: check if all selected answers are correct and all correct answers are selected
                const selectedValues = Array.from(selectedOptions).map(opt => opt.dataset.value);
                const allSelectedCorrect = selectedValues.every(val => answers.includes(val));
                const allCorrectSelected = answers.every(ans => selectedValues.includes(ans));
                isCorrect = allSelectedCorrect && allCorrectSelected;
                userAnswer = selectedValues.join(', ');
            } else {
                // Single select
                const selected = selectedOptions[0];
                userAnswer = selected.dataset.value;
                isCorrect = answers.includes(userAnswer);
            }

            // Mark options - data-value is already normalized
            card.querySelectorAll('.answer-option').forEach(opt => {
                opt.classList.remove('correct', 'incorrect');
                const optVal = opt.dataset.value;
                if (answers.includes(optVal)) {
                    opt.classList.add('correct');
                    opt.querySelector('.result-icon').textContent = '✓';
                } else if (opt.classList.contains('selected')) {
                    opt.classList.add('incorrect');
                    opt.querySelector('.result-icon').textContent = '✗';
                }
            });
        } else if (type === 'number') {
            const input = document.getElementById('input-' + projectId + '-' + questionId);
            userAnswer = input.value.trim();
            if (!userAnswer) {
                feedback.textContent = 'Please enter a number.';
                feedback.className = 'feedback show incorrect';
                return;
            }
            // For numbers, check if any answer matches (with tolerance)
            const userNum = parseFloat(userAnswer);
            isCorrect = answers.some(ans => {
                const ansNum = parseFloat(ans);
                return !isNaN(ansNum) && Math.abs(userNum - ansNum) < 0.001;
            });

            input.classList.remove('correct', 'incorrect');
            input.classList.add(isCorrect ? 'correct' : 'incorrect');
        } else if (type === 'freetext') {
            const input = document.getElementById('input-' + projectId + '-' + questionId);
            userAnswer = input.value.trim().toLowerCase();
            if (!userAnswer) {
                feedback.textContent = 'Please enter an answer.';
                feedback.className = 'feedback show incorrect';
                return;
            }
            // For free text, check if user answer matches any expected answer (case-insensitive)
            isCorrect = answers.some(ans => userAnswer === ans.toLowerCase());

            input.classList.remove('correct', 'incorrect');
            input.classList.add(isCorrect ? 'correct' : 'incorrect');
        }

        // Update feedback
        if (isCorrect) {
            feedback.textContent = i18n.feedbackCorrect;
            feedback.className = 'feedback show correct';
        } else {
            feedback.textContent = i18n.feedbackIncorrect;
            feedback.className = 'feedback show incorrect';
        }

        // Track score
        if (answeredQuestions[qKey] === undefined) {
            answeredQuestions[qKey] = isCorrect;
            if (isCorrect) correctCount++;
        } else {
            if (answeredQuestions[qKey] && !isCorrect) {
                correctCount--;
            } else if (!answeredQuestions[qKey] && isCorrect) {
                correctCount++;
            }
            answeredQuestions[qKey] = isCorrect;
        }

        updateScore();
    }

    // Mark manual answer (for purpose questions)
    function markManual(projectId, questionId, isCorrect) {
        const feedback = document.getElementById('feedback-' + projectId + '-' + questionId);
        const qKey = projectId + '-' + questionId;

        if (isCorrect) {
            feedback.textContent = i18n.feedbackCorrect;
            feedback.className = 'feedback show correct';
        } else {
            feedback.textContent = i18n.feedbackIncorrect;
            feedback.className = 'feedback show incorrect';
        }

        // Track score
        if (answeredQuestions[qKey] === undefined) {
            answeredQuestions[qKey] = isCorrect;
            if (isCorrect) correctCount++;
        } else {
            if (answeredQuestions[qKey] && !isCorrect) {
                correctCount--;
            } else if (!answeredQuestions[qKey] && isCorrect) {
                correctCount++;
            }
            answeredQuestions[qKey] = isCorrect;
        }

        updateScore();
    }

    // Update score display
    function updateScore() {
        document.getElementById('score-correct').textContent = correctCount;
        document.getElementById('score-total').textContent = totalCheckable;
        const pct = totalCheckable > 0 ? Math.round((correctCount / totalCheckable) * 100) : 0;
        document.getElementById('score-percentage').textContent = pct + '%';
    }

    // Toggle showing correct answer
    function toggleAnswer(projectId, questionId) {
        const answerEl = document.getElementById('answer-' + projectId + '-' + questionId);
        answerEl.classList.toggle('show');
    }

    // Show sprite code panel
    function showSpriteCode(projectId, spriteName) {
        const overlay = document.getElementById('code-overlay-' + projectId);
        const panel = document.getElementById('code-panel-' + projectId);
        const nameEl = document.getElementById('code-sprite-name-' + projectId);

        if (!panel || !overlay) {
            console.error('Code panel not found for project:', projectId);
            return;
        }

        overlay.classList.add('show');
        panel.classList.add('show');
        if (nameEl) nameEl.textContent = spriteName;

        showSpriteTab(projectId, spriteName);
    }

    // Hide sprite code panel
    function hideCodePanel(projectId) {
        const overlay = document.getElementById('code-overlay-' + projectId);
        const panel = document.getElementById('code-panel-' + projectId);
        if (overlay) overlay.classList.remove('show');
        if (panel) panel.classList.remove('show');
    }

    // Show specific sprite tab in code panel
    function showSpriteTab(projectId, spriteName) {
        const tabs = document.getElementById('code-tabs-' + projectId);
        const panel = document.getElementById('code-panel-' + projectId);
        const nameEl = document.getElementById('code-sprite-name-' + projectId);
        const sanitizedName = spriteName.replace(/[^a-zA-Z0-9]/g, '-');

        if (!tabs || !panel) return;

        // Update sprite name in header
        if (nameEl) nameEl.textContent = spriteName;

        // Update tab active state
        tabs.querySelectorAll('.sprite-tab').forEach(tab => {
            tab.classList.toggle('active', tab.dataset.sprite === spriteName);
        });

        // Show correct code view
        panel.querySelectorAll('.sprite-code-view').forEach(view => {
            view.classList.add('hidden');
        });

        const codeView = document.getElementById('code-' + projectId + '-' + sanitizedName);
        if (codeView) {
            codeView.classList.remove('hidden');

            // Render scratchblocks if not already rendered
            const pre = codeView.querySelector('pre.sprite-code-blocks');
            if (pre && !pre.classList.contains('rendered')) {
                renderAndTranslate('#code-' + projectId + '-' + sanitizedName + ' pre.blocks', {
                    style: 'scratch3',
                    scale: 0.7
                });
                pre.classList.add('rendered');
            }
        }
    }

    // Export answers as JSON (for export mode)
    function exportAnswers() {
        const answers = [];
        const projectName = "$unsafe{model.projects().isEmpty() ? "" : model.projects().get(0).name().replace("\"", "\\\"")}";

        // Collect all answers from all question cards
        document.querySelectorAll('.question-card').forEach(card => {
            const questionId = parseInt(card.dataset.questionId);
            const questionType = card.dataset.type;
            const finder = card.dataset.finder;
            const sprite = card.dataset.sprite;

            // Get question text from the rendered content
            const questionTextEl = card.querySelector('.question-text');
            const questionText = questionTextEl ? questionTextEl.textContent.trim() : '';

            // Get the correct answers from the data attribute (for evaluation)
            let correctAnswers = [];
            try {
                if (card.dataset.translatableAnswers) {
                    correctAnswers = JSON.parse(card.dataset.translatableAnswers);
                }
            } catch (e) {
                console.error('Failed to parse translatable answers for question', questionId, e);
                // Fallback to plain strings if parsing fails
                correctAnswers = card.dataset.answers ? card.dataset.answers.split('|').filter(a => a) : [];
            }

            // Get choices for multiple choice questions
            let choices = [];
            if (questionType === 'MULTIPLE_CHOICE' || questionType === 'YES_NO') {
                const optionEls = card.querySelectorAll('.answer-option');
                choices = Array.from(optionEls).map(opt => ({
                    value: opt.dataset.value,
                    text: opt.querySelector('.option-text')?.textContent?.trim() || opt.dataset.value
                }));
            }

            let studentAnswer = null;

            if (questionType === 'MULTIPLE_CHOICE') {
                const isMultiSelect = card.dataset.multiselect === 'true';
                if (isMultiSelect) {
                    // Multi-select: collect all selected options
                    const selectedOptions = card.querySelectorAll('.answer-option.selected');
                    if (selectedOptions.length > 0) {
                        const values = Array.from(selectedOptions).map(opt =>
                                opt.dataset.value || opt.querySelector('.option-text')?.textContent?.trim()
                        );
                        studentAnswer = values.join('|');
                    }
                } else {
                    // Single-select
                    const selected = card.querySelector('.answer-option.selected');
                    if (selected) {
                        studentAnswer = selected.dataset.value || selected.querySelector('.option-text')?.textContent?.trim();
                    }
                }
            } else if (questionType === 'YES_NO') {
                const selected = card.querySelector('.answer-option.selected');
                if (selected) {
                    studentAnswer = selected.dataset.value;
                }
            } else if (questionType === 'NUMBER' || questionType === 'FREE_TEXT') {
                const input = card.querySelector('input, textarea');
                if (input && input.value.trim()) {
                    studentAnswer = input.value.trim();
                }
            }

            answers.push({
                questionId: questionId,
                finder: finder,
                sprite: sprite,
                questionType: questionType,
                questionText: questionText,
                choices: choices,
                correctAnswers: correctAnswers,
                studentAnswer: studentAnswer
            });
        });

        const exportData = {
            projectFile: projectName,
            exportedAt: new Date().toISOString(),
            language: '${model.language()}',
            answers: answers
        };

        // Download as JSON file
        const blob = new Blob([JSON.stringify(exportData, null, 2)], {type: 'application/json'});
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = projectName.replace(/\.[^/.]+$/, '') + '_answers.json';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);

        // Update status
        const statusEl = document.getElementById('export-status');
        if (statusEl) {
            statusEl.textContent = '✓ ' + answers.length + ' ${model.translator().getInfo("questions_answers_exported")}';
            statusEl.style.color = '#16a34a';
        }
    }
</script>
</body>
</html>
