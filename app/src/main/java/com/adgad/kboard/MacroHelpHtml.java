package com.adgad.kboard;

/**
 * Turns the bundled macro guide into a small HTML subset a TextView can show.
 * The guide file in the repository stays the editable source.
 */
final class MacroHelpHtml {

    private MacroHelpHtml() {
    }

    static String toHtml(String markdown) {
        String[] lines = markdown.replace("\r\n", "\n").split("\n", -1);
        StringBuilder html = new StringBuilder();
        boolean inCode = false;
        boolean inList = false;
        StringBuilder paragraph = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("```")) {
                flushParagraph(html, paragraph);
                if (inList) {
                    html.append("</ul>");
                    inList = false;
                }
                if (inCode) {
                    html.append("</pre>");
                    inCode = false;
                } else {
                    html.append("<pre>");
                    inCode = true;
                }
                continue;
            }
            if (inCode) {
                html.append(escape(line)).append('\n');
                continue;
            }
            if (line.trim().isEmpty()) {
                flushParagraph(html, paragraph);
                if (inList) {
                    html.append("</ul>");
                    inList = false;
                }
                continue;
            }
            if (line.startsWith("### ")) {
                flushParagraph(html, paragraph);
                closeList(html, inList);
                inList = false;
                html.append("<h3>").append(inline(line.substring(4))).append("</h3>");
                continue;
            }
            if (line.startsWith("## ")) {
                flushParagraph(html, paragraph);
                closeList(html, inList);
                inList = false;
                html.append("<h2>").append(inline(line.substring(3))).append("</h2>");
                continue;
            }
            if (line.startsWith("# ")) {
                flushParagraph(html, paragraph);
                closeList(html, inList);
                inList = false;
                html.append("<h1>").append(inline(line.substring(2))).append("</h1>");
                continue;
            }
            if (line.startsWith("|")) {
                flushParagraph(html, paragraph);
                closeList(html, inList);
                inList = false;
                html.append("<p>").append(inline(line)).append("</p>");
                continue;
            }
            if (line.startsWith("- ")) {
                flushParagraph(html, paragraph);
                if (!inList) {
                    html.append("<ul>");
                    inList = true;
                }
                html.append("<li>").append(inline(line.substring(2))).append("</li>");
                continue;
            }
            if (inList) {
                html.append("</ul>");
                inList = false;
            }
            if (paragraph.length() > 0) {
                paragraph.append(' ');
            }
            paragraph.append(line.trim());
        }
        flushParagraph(html, paragraph);
        closeList(html, inList);
        if (inCode) {
            html.append("</pre>");
        }
        return html.toString();
    }

    private static void flushParagraph(StringBuilder html, StringBuilder paragraph) {
        if (paragraph.length() == 0) {
            return;
        }
        html.append("<p>").append(inline(paragraph.toString())).append("</p>");
        paragraph.setLength(0);
    }

    private static void closeList(StringBuilder html, boolean inList) {
        if (inList) {
            html.append("</ul>");
        }
    }

    static String inline(String text) {
        StringBuilder out = new StringBuilder();
        int index = 0;
        while (index < text.length()) {
            int code = text.indexOf('`', index);
            int link = text.indexOf('[', index);
            int next = nextMarker(code, link);
            if (next < 0) {
                out.append(escape(text.substring(index)));
                break;
            }
            out.append(escape(text.substring(index, next)));
            if (next == code) {
                int end = text.indexOf('`', next + 1);
                if (end < 0) {
                    out.append(escape(text.substring(next)));
                    break;
                }
                out.append("<code>").append(escape(text.substring(next + 1, end))).append("</code>");
                index = end + 1;
            } else {
                int labelEnd = text.indexOf("](", next);
                int urlEnd = labelEnd < 0 ? -1 : text.indexOf(')', labelEnd + 2);
                if (labelEnd < 0 || urlEnd < 0) {
                    out.append(escape(text.substring(next)));
                    break;
                }
                String label = text.substring(next + 1, labelEnd);
                String url = text.substring(labelEnd + 2, urlEnd);
                if (url.startsWith("https://") || url.startsWith("http://")) {
                    out.append("<a href=\"").append(escape(url)).append("\">")
                            .append(escape(label)).append("</a>");
                } else {
                    out.append(escape(label));
                }
                index = urlEnd + 1;
            }
        }
        return out.toString();
    }

    private static int nextMarker(int code, int link) {
        if (code < 0) {
            return link;
        }
        if (link < 0) {
            return code;
        }
        return Math.min(code, link);
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
