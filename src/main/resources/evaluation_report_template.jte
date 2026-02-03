@import de.uni_passau.fim.se2.litterbox.report.EvaluationResultHandler.EvaluationTemplateModel
@import de.uni_passau.fim.se2.litterbox.report.EvaluationResultHandler.EvaluatedProject
@import de.uni_passau.fim.se2.litterbox.report.EvaluationResultHandler.EvaluatedQuestion
@import de.uni_passau.fim.se2.litterbox.analytics.questions.ProgramDimension
@import de.uni_passau.fim.se2.litterbox.analytics.questions.ScopeOfFocus
@import de.uni_passau.fim.se2.litterbox.analytics.questions.AnswerCheckResult.DimensionScore
@import gg.jte.support.ForSupport
@import java.util.Map

@param EvaluationTemplateModel model

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
    <title>${model.translator().getInfo("evaluation_title")} ${model.projects().isEmpty() ? "LitterBox" : model.projects().get(0).projectName()}</title>
    <link rel="icon" href="${model.favicon()}">
    <script>$unsafe{model.scripts().scratchBlocks()}</script>
    <script>$unsafe{model.scripts().scratchBlocksTranslations()}</script>
    <style>
        :root {
            --color-text: #4d97ff;
            --color-execution: #ff8c1a;
            --color-purpose: #40bf4a;
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
            max-width: 1200px;
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

        /* Score Summary */
        .score-summary {
            text-align: center;
            padding: 2rem;
            margin-bottom: 1.5rem;
        }

        .score-big {
            font-size: 3rem;
            font-weight: bold;
            margin-bottom: 0.5rem;
        }

        .score-big.good {
            color: var(--color-correct);
        }

        .score-big.partial {
            color: var(--color-partial);
        }

        .score-big.poor {
            color: var(--color-incorrect);
        }

        .score-detail {
            color: #666;
            font-size: 1.1rem;
        }

        /* Block Model Matrix */
        .matrix-section {
            margin-bottom: 2rem;
        }

        .matrix-section h3 {
            margin-top: 0;
            margin-bottom: 1rem;
            font-size: 1.1em;
            color: #555;
        }

        .score-matrices {
            display: flex;
            gap: 2rem;
            flex-wrap: wrap;
        }

        .matrix-table {
            border-collapse: collapse;
            font-size: 0.9em;
        }

        .matrix-table th,
        .matrix-table td {
            border: 1px solid #ddd;
            padding: 10px 16px;
            text-align: center;
        }

        .matrix-table th {
            background: #f8f9fa;
            font-weight: 600;
        }

        .cell-score {
            font-weight: 500;
        }

        .cell-score.good {
            color: var(--color-correct);
        }

        .cell-score.partial {
            color: var(--color-partial);
        }

        .cell-score.poor {
            color: var(--color-incorrect);
        }

        /* Filter Buttons */
        .filters {
            display: flex;
            gap: 10px;
            margin-bottom: 1.5rem;
            flex-wrap: wrap;
        }

        .filter-btn {
            background: #fff;
            border: 1px solid #ddd;
            padding: 8px 16px;
            border-radius: 20px;
            cursor: pointer;
            font-size: 0.95em;
            color: #555;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .filter-btn:hover {
            background: #f8f9fa;
            border-color: #bbb;
        }

        .filter-btn.active {
            color: #fff;
            border-color: transparent;
        }

        .filter-btn.active.all {
            background: #666;
        }

        .filter-btn.active.correct {
            background: var(--color-correct);
        }

        .filter-btn.active.incorrect {
            background: var(--color-incorrect);
        }

        .filter-btn.active.unanswered {
            background: var(--color-unanswered);
        }

        .filter-count {
            background: rgba(0, 0, 0, 0.1);
            padding: 2px 8px;
            border-radius: 10px;
            font-size: 0.85em;
        }

        .filter-btn.active .filter-count {
            background: rgba(255, 255, 255, 0.25);
        }

        /* Question Cards */
        .question-card {
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            margin-bottom: 1rem;
            overflow: hidden;
            display: block;
        }

        .question-card.hidden {
            display: none;
        }

        .question-card.correct {
            border-left: 4px solid var(--color-correct);
        }

        .question-card.incorrect {
            border-left: 4px solid var(--color-incorrect);
        }

        .question-card.partial-credit {
            border-left: 4px solid var(--color-partial);
        }

        .question-card.unanswered {
            border-left: 4px solid var(--color-unanswered);
        }

        .question-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem;
            background: #f8f9fa;
            border-bottom: 1px solid #e0e0e0;
        }

        .question-meta {
            display: flex;
            gap: 0.75rem;
            align-items: center;
        }

        .question-number {
            font-weight: 600;
            color: #333;
        }

        .dimension-badge {
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 0.8em;
            font-weight: 500;
        }

        .dimension-badge.TEXT {
            background: #4d97ff22;
            color: #4d97ff;
        }

        .dimension-badge.EXECUTION {
            background: #ff8c1a22;
            color: #ff8c1a;
        }

        .dimension-badge.PURPOSE {
            background: #40bf4a22;
            color: #40bf4a;
        }

        .result-indicator {
            font-size: 1.5rem;
        }

        .result-indicator.correct {
            color: var(--color-correct);
        }

        .result-indicator.incorrect {
            color: var(--color-incorrect);
        }

        .result-indicator.partial {
            color: var(--color-partial);
        }

        .result-indicator.unanswered {
            color: var(--color-unanswered);
        }

        .question-body {
            padding: 1rem;
        }

        .question-text {
            font-size: 1rem;
            margin-bottom: 1rem;
            line-height: 1.5;
        }

        .code-context {
            background: #f8f9fa;
            border-radius: 4px;
            padding: 1rem;
            margin-bottom: 1rem;
            overflow-x: auto;
        }

        .answer-section {
            margin-top: 1rem;
            padding-top: 1rem;
            border-top: 1px solid #eee;
        }

        .answer-row {
            display: flex;
            gap: 1rem;
            margin-bottom: 0.5rem;
            align-items: baseline;
        }

        .answer-label {
            font-weight: 500;
            color: #666;
            min-width: 120px;
        }

        .answer-value {
            flex: 1;
        }

        .answer-value.provided {
            font-weight: 500;
        }

        .answer-value.correct-answer {
            color: var(--color-correct);
        }

        .answer-value.wrong-answer {
            color: var(--color-incorrect);
        }

        .feedback-box {
            background: #fef3c7;
            border: 1px solid #fcd34d;
            border-radius: 4px;
            padding: 0.75rem 1rem;
            margin-top: 0.75rem;
            font-size: 0.9em;
        }

        .feedback-box.llm {
            background: #ede9fe;
            border-color: #a78bfa;
        }

        .partial-credit-badge {
            display: inline-block;
            background: var(--color-partial);
            color: white;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 0.85em;
            font-weight: 500;
            margin-left: 0.5rem;
        }

        /* Scratchblocks styling */
        .blocks {
            background: #f8f9fa;
            padding: 1rem;
            border-radius: 4px;
        }

        code.scratchblocks {
            background: #f0f0f0;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: inherit;
        }
    </style>
</head>
<body>
<div class="container">
    <header class="report-header boxed">
        @if(!model.logo().isEmpty())
            <div class="logo">$unsafe{model.logo()}</div>
        @endif
        <h1>${model.translator().getInfo("evaluation_title")} ${model.projects().isEmpty() ? "" : model.projects().get(0).projectName()}</h1>
    </header>

    @for(var project : ForSupport.of(model.projects()))
        !{var checkResult = project.get().checkResult();}
        !{var scorePercent = (int)(checkResult.score() * 100);}
        !{var scoreClass = scorePercent >= 70 ? "good" : (scorePercent >= 40 ? "partial" : "poor");}

        <section class="boxed score-summary">
            <div class="score-big ${scoreClass}">${scorePercent}%</div>
            <div class="score-detail">
                ${checkResult.correctAnswers()}
                / ${checkResult.totalQuestions()} ${model.translator().getInfo("evaluation_questions_correct")}
            </div>
            <div style="margin-top: 0.5rem; color: #888; font-size: 0.9em;">
                ${project.get().projectName()}
            </div>
        </section>

        <section class="boxed matrix-section">
            <h3>${model.translator().getInfo("evaluation_breakdown")}</h3>
            <div class="score-matrices">
                <div>
                    <h4 style="margin-top: 0; font-size: 0.9em; color: #666;">${model.translator().getInfo("evaluation_by_dimension")}</h4>
                    <table class="matrix-table">
                        <thead>
                        <tr>
                            <th>${model.translator().getInfo("block_model_dimension")}</th>
                            <th>${model.translator().getInfo("evaluation_score")}</th>
                        </tr>
                        </thead>
                        <tbody>
                        @for(var dim : ProgramDimension.values())
                            !{var dimScore = checkResult.byProgramDimension().get(dim);}
                            @if(dimScore != null && dimScore.total() > 0)
                                !{var dimPercent = (int)(dimScore.percentage() * 100);}
                                !{var dimClass = dimPercent >= 70 ? "good" : (dimPercent >= 40 ? "partial" : "poor");}
                                <tr>
                                    <td><span class="dimension-badge ${dim.name()}">${dim.name()}</span></td>
                                    <td class="cell-score ${dimClass}">${dimScore.correct()}/${dimScore.total()}</td>
                                </tr>
                            @endif
                        @endfor
                        </tbody>
                    </table>
                </div>
                <div>
                    <h4 style="margin-top: 0; font-size: 0.9em; color: #666;">${model.translator().getInfo("evaluation_by_scope")}</h4>
                    <table class="matrix-table">
                        <thead>
                        <tr>
                            <th>${model.translator().getInfo("block_model_scope")}</th>
                            <th>${model.translator().getInfo("evaluation_score")}</th>
                        </tr>
                        </thead>
                        <tbody>
                        @for(var scope : ScopeOfFocus.values())
                            !{var scopeScore = checkResult.byScopeOfFocus().get(scope);}
                            @if(scopeScore != null && scopeScore.total() > 0)
                                !{var scopePercent = (int)(scopeScore.percentage() * 100);}
                                !{var scopeClass = scopePercent >= 70 ? "good" : (scopePercent >= 40 ? "partial" : "poor");}
                                <tr>
                                    <td>${scope.name()}</td>
                                    <td class="cell-score ${scopeClass}">${scopeScore.correct()}
                                        /${scopeScore.total()}</td>
                                </tr>
                            @endif
                        @endfor
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <section class="boxed">
            <h3 style="margin-top: 0;">${model.translator().getInfo("evaluation_details")}</h3>

            <div class="filters">
                <button class="filter-btn active all" data-filter="all">
                    ${model.translator().getInfo("filter_all")} <span class="filter-count" id="count-all">0</span>
                </button>
                <button class="filter-btn correct" data-filter="correct">
                    ${model.translator().getInfo("filter_correct")} <span class="filter-count"
                                                                          id="count-correct">0</span>
                </button>
                <button class="filter-btn incorrect" data-filter="incorrect">
                    ${model.translator().getInfo("filter_incorrect")} <span class="filter-count"
                                                                            id="count-incorrect">0</span>
                </button>
                <button class="filter-btn unanswered" data-filter="unanswered">
                    ${model.translator().getInfo("filter_unanswered")} <span class="filter-count" id="count-unanswered">0</span>
                </button>
            </div>

            @for(var question : ForSupport.of(project.get().questions()))
                !{var isCorrect = question.get().correct();}
                !{var hasPartial = !isCorrect && question.get().partialCredit() > 0;}
                !{var isUnanswered = question.get().providedAnswer() == null || question.get().providedAnswer().isEmpty();}
                !{var status = isCorrect ? "correct" : (isUnanswered ? "unanswered" : "incorrect");}
                !{var cardClass = isCorrect ? "correct" : (hasPartial ? "partial-credit" : (isUnanswered ? "unanswered" : "incorrect"));}
                !{var indicatorClass = cardClass;}
                !{var indicator = isCorrect ? "✓" : (hasPartial ? "~" : (isUnanswered ? "?" : "✗"));}

                <div class="question-card ${cardClass}" data-status="${status}">
                    <div class="question-header">
                        <div class="question-meta">
                            <span class="question-number">Q${question.get().id()}</span>
                            <span class="dimension-badge ${question.get().programDimension().name()}">${question.get().programDimension().name()}</span>
                            <span style="color: #888; font-size: 0.85em;">${question.get().scopeOfFocus().name()}</span>
                        </div>
                        <div class="result-indicator ${indicatorClass}">
                            ${indicator}
                            @if(hasPartial)
                                <span class="partial-credit-badge">${(int)(question.get().partialCredit() * 100)}%</span>
                            @endif
                        </div>
                    </div>
                    <div class="question-body">
                        <div class="question-text">$unsafe{question.get().questionText()}</div>

                        @if(question.get().scratchBlocks() != null && !question.get().scratchBlocks().isEmpty())
                            <div class="code-context">
                                <pre class="blocks">$unsafe{question.get().scratchBlocks()}</pre>
                            </div>
                        @endif

                        <div class="answer-section">
                            <div class="answer-row">
                                <span class="answer-label">${model.translator().getInfo("evaluation_your_answer")}:</span>
                                <span class="answer-value provided ${isCorrect ? "correct-answer" : "wrong-answer"}">
                                @if(question.get().providedAnswer() != null && !question.get().providedAnswer().isEmpty())
                                    $unsafe{question.get().providedAnswer()}
                                @else
                                    <em style="color: #999;">${model.translator().getInfo("evaluation_no_answer")}</em>
                                @endif
                            </span>
                            </div>
                            @if(!isCorrect && question.get().expectedAnswer() != null)
                                <div class="answer-row">
                                    <span class="answer-label">${model.translator().getInfo("evaluation_expected")}:</span>
                                    <span class="answer-value correct-answer">$unsafe{question.get().expectedAnswer()}</span>
                                </div>
                            @endif
                        </div>

                        @if(question.get().feedback() != null && !question.get().feedback().isEmpty())
                            <div class="feedback-box ${question.get().llmEvaluation() != null ? "llm" : ""}">
                                @if(question.get().llmEvaluation() != null)
                                    <strong>🤖 ${model.translator().getInfo("evaluation_llm_feedback")}:</strong>
                                @else
                                    <strong>${model.translator().getInfo("evaluation_feedback")}:</strong>
                                @endif
                                $unsafe{question.get().feedback()}
                            </div>
                        @endif
                    </div>
                </div>
            @endfor
        </section>
    @endfor
</div>

<script>
    document.addEventListener('DOMContentLoaded', function () {
        // Helper functions to render scratchblocks with translation to target language
        const targetLang = '${model.language()}';

        function renderAndTranslate(selector) {
            document.querySelectorAll(selector).forEach(el => {
                const code = el.textContent;
                if (!code.trim()) return;
                const doc = scratchblocks.parse(code, {languages: ['en']});
                if (targetLang !== 'en' && scratchblocks.allLanguages[targetLang]) {
                    doc.translate(scratchblocks.allLanguages[targetLang]);
                }
                const view = scratchblocks.newView(doc, {style: 'scratch3', scale: 0.7});
                el.innerHTML = '';
                el.appendChild(view.render());
            });
        }

        function renderInlineAndTranslate(selector) {
            document.querySelectorAll(selector).forEach(el => {
                const code = el.textContent;
                if (!code.trim()) return;
                const doc = scratchblocks.parse(code, {languages: ['en'], inline: true});
                if (targetLang !== 'en' && scratchblocks.allLanguages[targetLang]) {
                    doc.translate(scratchblocks.allLanguages[targetLang]);
                }
                const view = scratchblocks.newView(doc, {style: 'scratch3', scale: 0.7});
                el.innerHTML = '';
                el.appendChild(view.render());
            });
        }

        // Render scratchblocks with translation
        renderAndTranslate('pre.blocks');
        renderInlineAndTranslate('code.scratchblocks');

        // Filtering logic
        const cards = document.querySelectorAll('.question-card');
        const buttons = document.querySelectorAll('.filter-btn');

        // Calculate initial counts
        const counts = {
            all: cards.length,
            correct: 0,
            incorrect: 0,
            unanswered: 0
        };

        cards.forEach(card => {
            const status = card.dataset.status;
            if (status === 'correct') counts.correct++;
            else if (status === 'unanswered') counts.unanswered++;
            else counts.incorrect++;
        });

        // Update count displays
        document.getElementById('count-all').textContent = counts.all;
        document.getElementById('count-correct').textContent = counts.correct;
        document.getElementById('count-incorrect').textContent = counts.incorrect;
        document.getElementById('count-unanswered').textContent = counts.unanswered;

        // Handle button clicks
        buttons.forEach(btn => {
            btn.addEventListener('click', () => {
                const filter = btn.dataset.filter;

                // Update active state
                buttons.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');

                // Filter cards
                cards.forEach(card => {
                    if (filter === 'all' || card.dataset.status === filter) {
                        card.classList.remove('hidden');
                    } else {
                        if (filter === 'incorrect' && card.dataset.status !== 'correct' && card.dataset.status !== 'unanswered') {
                            // Group any non-correct, non-unanswered as incorrect (e.g. partial)
                            card.classList.remove('hidden');
                        } else {
                            card.classList.add('hidden');
                        }
                    }
                });
            });
        });
    });
</script>
</body>
</html>
