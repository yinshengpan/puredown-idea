// PureDown In-Place Preview Engine
(function() {
    let isUserScrolling = false;
    let scrollTimeout = null;

    // Lightbox modal creation
    function ensureLightbox() {
        let lightbox = document.getElementById('lightbox-modal');
        if (!lightbox) {
            lightbox = document.createElement('div');
            lightbox.id = 'lightbox-modal';
            lightbox.className = 'lightbox-modal';
            lightbox.innerHTML = '<img id="lightbox-img" src="" alt="Enlarged preview" />';
            lightbox.addEventListener('click', function() {
                lightbox.classList.remove('active');
            });
            document.body.appendChild(lightbox);

            document.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    lightbox.classList.remove('active');
                }
            });
        }
        return lightbox;
    }

    window.openLightbox = function(img) {
        const modal = ensureLightbox();
        const modalImg = document.getElementById('lightbox-img');
        if (modal && modalImg && img) {
            modalImg.src = img.src;
            modal.classList.add('active');
        }
    };

    window.copyCode = function(button) {
        const codeWrapper = button.closest('.code-block-wrapper');
        if (!codeWrapper) return;
        const codeElem = codeWrapper.querySelector('pre code');
        if (!codeElem) return;

        const text = codeElem.innerText;
        navigator.clipboard.writeText(text).then(function() {
            const originalText = button.innerHTML;
            const isEnglish = originalText.indexOf('Copy') !== -1;
            button.innerHTML = isEnglish ? '✅ Copied' : '✅ 已复制';
            button.style.borderColor = '#2e7d32';
            setTimeout(function() {
                button.innerHTML = originalText;
                button.style.borderColor = '';
            }, 2000);
        }).catch(function(err) {
            console.error('Copy failed:', err);
        });
    };

    window.updateContent = function(html) {
        const container = document.getElementById('puredown-content');
        if (!container) return;

        // Remember current scroll position
        const scrollY = window.scrollY;
        
        container.innerHTML = html;

        // Run syntax highlighting if hljs exists
        if (window.hljs) {
            try {
                container.querySelectorAll('pre code').forEach(function(el) {
                    hljs.highlightElement(el);
                });
            } catch (e) {
                console.warn('Highlight.js error:', e);
            }
        }

        // Initialize Mermaid diagrams if any
        if (window.mermaid) {
            try {
                mermaid.init(undefined, container.querySelectorAll('.mermaid'));
            } catch (e) {
                console.warn('Mermaid rendering error:', e);
            }
        }

        // Initialize KaTeX math formulas if available
        if (window.renderMathInElement) {
            try {
                renderMathInElement(container, {
                    delimiters: [
                        {left: '$$', right: '$$', display: true},
                        {left: '$', right: '$', display: false}
                    ],
                    throwOnError: false
                });
            } catch (e) {
                console.warn('KaTeX rendering error:', e);
            }
        }

        // Restore scroll position
        window.scrollTo(0, scrollY);
    };

    window.setTheme = function(isDark) {
        if (isDark) {
            document.body.classList.add('theme-dark');
        } else {
            document.body.classList.remove('theme-dark');
        }
    };

    window.scrollToLine = function(targetLine) {
        if (isUserScrolling) return;

        // Find the element with closest data-source-line <= targetLine
        const elements = document.querySelectorAll('[data-source-line]');
        let bestElem = null;
        let bestLine = -1;

        for (let i = 0; i < elements.length; i++) {
            const line = parseInt(elements[i].getAttribute('data-source-line'), 10);
            if (line <= targetLine && line > bestLine) {
                bestLine = line;
                bestElem = elements[i];
            }
        }

        if (bestElem) {
            bestElem.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } else if (targetLine === 0) {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    };

    // Listen for user clicks on checkboxes and wikilinks
    document.addEventListener('click', function(e) {
        const target = e.target;

        // Handle task-list checkbox clicks
        if (target && target.type === 'checkbox' && target.hasAttribute('data-source-line')) {
            const line = parseInt(target.getAttribute('data-source-line'), 10);
            const checked = target.checked;
            if (window.pureDownBridge && window.pureDownBridge.toggleCheckbox) {
                window.pureDownBridge.toggleCheckbox(line, checked);
            } else if (window.cefQuery) {
                window.cefQuery({
                    request: 'toggleCheckbox:' + line + ':' + checked,
                    onSuccess: function() {},
                    onFailure: function() {}
                });
            }
        }

        // Handle WikiLinks [[...]]
        const link = target.closest('a.wikilink');
        if (link) {
            e.preventDefault();
            const targetNote = link.getAttribute('data-target-note');
            if (window.pureDownBridge && window.pureDownBridge.openWikiLink) {
                window.pureDownBridge.openWikiLink(targetNote);
            } else if (window.cefQuery) {
                window.cefQuery({
                    request: 'openWikiLink:' + targetNote,
                    onSuccess: function() {},
                    onFailure: function() {}
                });
            }
        }
    });

    // Detect user scroll
    window.addEventListener('scroll', function() {
        isUserScrolling = true;
        clearTimeout(scrollTimeout);
        scrollTimeout = setTimeout(function() {
            isUserScrolling = false;
        }, 150);
    }, { passive: true });

    document.addEventListener('DOMContentLoaded', function() {
        ensureLightbox();
    });

})();
