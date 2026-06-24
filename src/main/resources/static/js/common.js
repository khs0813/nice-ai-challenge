(function () {
    function toggleTarget(button) {
        var targetId = button.getAttribute('data-toggle-target');
        if (!targetId) {
            return;
        }
        var target = document.getElementById(targetId);
        if (!target) {
            return;
        }
        target.classList.toggle('is-collapsed');
        button.setAttribute('aria-expanded', target.classList.contains('is-collapsed') ? 'false' : 'true');
    }

    function syncCheckbox(checkbox) {
        var targetId = checkbox.getAttribute('data-sync-checkbox');
        if (!targetId) {
            return;
        }
        var target = document.getElementById(targetId);
        if (!target) {
            return;
        }
        target.value = checkbox.checked ? 'Y' : 'N';
    }

    function appendInlineTokens(parent, text) {
        var pattern = /(\*\*[^*]+\*\*|`[^`]+`)/g;
        var lastIndex = 0;
        var match;
        while ((match = pattern.exec(text)) !== null) {
            if (match.index > lastIndex) {
                parent.appendChild(document.createTextNode(text.slice(lastIndex, match.index)));
            }
            var token = match[0];
            if (token.indexOf('**') === 0) {
                var strong = document.createElement('strong');
                strong.textContent = token.slice(2, -2);
                parent.appendChild(strong);
            } else {
                var code = document.createElement('code');
                code.textContent = token.slice(1, -1);
                parent.appendChild(code);
            }
            lastIndex = pattern.lastIndex;
        }
        if (lastIndex < text.length) {
            parent.appendChild(document.createTextNode(text.slice(lastIndex)));
        }
    }

    function appendInlineMarkdown(parent, text) {
        var codeFencePattern = /``\s*([A-Za-z0-9_-]+)\s+([\s\S]*?)\s*``/g;
        var lastIndex = 0;
        var match;
        while ((match = codeFencePattern.exec(text)) !== null) {
            if (match.index > lastIndex) {
                appendInlineTokens(parent, text.slice(lastIndex, match.index));
            }
            appendCodeBlock(parent, match[1], match[2]);
            lastIndex = codeFencePattern.lastIndex;
        }
        if (lastIndex < text.length) {
            appendInlineTokens(parent, text.slice(lastIndex));
        }
    }

    function appendParagraph(container, text) {
        if (!text.trim()) {
            return;
        }
        var paragraph = document.createElement('p');
        appendInlineMarkdown(paragraph, text.trim());
        container.appendChild(paragraph);
    }

    function normalizeFenceLine(line) {
        var singleLineDoubleFence = line.match(/^\s*``\s*([A-Za-z0-9_-]+)\s+([\s\S]*?)\s*``\s*$/);
        if (singleLineDoubleFence) {
            return {
                type: 'singleCode',
                lang: singleLineDoubleFence[1],
                code: singleLineDoubleFence[2]
            };
        }
        var singleLineTripleFence = line.match(/^\s*```\s*([A-Za-z0-9_-]+)?\s+([\s\S]*?)\s*```\s*$/);
        if (singleLineTripleFence) {
            return {
                type: 'singleCode',
                lang: singleLineTripleFence[1] || '',
                code: singleLineTripleFence[2]
            };
        }
        return null;
    }

    function appendCodeBlock(container, lang, codeText) {
        var wrap = document.createElement('div');
        wrap.className = 'markdown-code-block';
        if (lang) {
            var label = document.createElement('div');
            label.className = 'markdown-code-label';
            label.textContent = lang.toUpperCase();
            wrap.appendChild(label);
        }
        var pre = document.createElement('pre');
        var code = document.createElement('code');
        code.textContent = codeText;
        pre.appendChild(code);
        wrap.appendChild(pre);
        container.appendChild(wrap);
    }

    function renderMarkdownBlock(source) {
        var text = normalizeMarkdownText(source.textContent || '');
        var lines = text.replace(/\r\n/g, '\n').split('\n');
        var container = document.createElement('div');
        var list = null;
        var paragraphLines = [];
        var inCode = false;
        var codeLang = '';
        var codeLines = [];

        function flushParagraph() {
            if (paragraphLines.length > 0) {
                appendParagraph(container, paragraphLines.join(' '));
                paragraphLines = [];
            }
        }

        function flushList() {
            if (list) {
                container.appendChild(list);
                list = null;
            }
        }

        function flushCode() {
            appendCodeBlock(container, codeLang, codeLines.join('\n'));
            codeLines = [];
            codeLang = '';
        }

        lines.forEach(function (line) {
            var normalizedFence = normalizeFenceLine(line);
            if (normalizedFence) {
                flushParagraph();
                flushList();
                appendCodeBlock(container, normalizedFence.lang, normalizedFence.code);
                return;
            }

            var fence = line.match(/^```([A-Za-z0-9_-]*)\s*$/);
            if (fence) {
                if (inCode) {
                    flushCode();
                    inCode = false;
                } else {
                    flushParagraph();
                    flushList();
                    inCode = true;
                    codeLang = fence[1] || '';
                    codeLines = [];
                }
                return;
            }
            if (inCode) {
                codeLines.push(line);
                return;
            }

            var bullet = line.match(/^\s*[-*]\s+(.+)$/);
            if (bullet) {
                flushParagraph();
                if (!list) {
                    list = document.createElement('ul');
                }
                var item = document.createElement('li');
                appendInlineMarkdown(item, bullet[1]);
                list.appendChild(item);
                return;
            }

            if (!line.trim()) {
                flushParagraph();
                flushList();
                return;
            }
            flushList();
            paragraphLines.push(line.trim());
        });

        if (inCode) {
            flushCode();
        }
        flushParagraph();
        flushList();
        container.className = 'markdown-message';
        if (source.classList.contains('markdown-compact')) {
            container.classList.add('markdown-compact');
        }
        if (source.classList.contains('markdown-alert')) {
            container.classList.add('markdown-alert');
        }
        source.replaceWith(container);
    }

    function normalizeMarkdownText(text) {
        return text
                .replace(/\r\n/g, '\n')
                .replace(/^(\s*)(PASS|WARN|FAIL)\s+-\s+/i, '$1$2\n- ')
                .replace(/\s+-\s+(\*\*[^*]+\*\*:)/g, '\n- $1');
    }

    document.querySelectorAll('[data-sync-checkbox]').forEach(function (checkbox) {
        var target = document.getElementById(checkbox.getAttribute('data-sync-checkbox'));
        if (target) {
            checkbox.checked = target.value === 'Y';
        }
    });

    document.querySelectorAll('[data-render-markdown]').forEach(renderMarkdownBlock);

    document.addEventListener('click', function (event) {
        var button = event.target.closest('[data-toggle-target]');
        if (button) {
            toggleTarget(button);
        }
    });

    document.addEventListener('change', function (event) {
        var checkbox = event.target.closest('[data-sync-checkbox]');
        if (checkbox) {
            syncCheckbox(checkbox);
        }
    });
})();
