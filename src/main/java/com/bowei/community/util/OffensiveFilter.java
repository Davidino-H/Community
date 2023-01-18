package com.bowei.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class OffensiveFilter {
    private static final String REPLACEMENT = "***";
    //根结点
    private TrieNode rootNode = new TrieNode();
    @PostConstruct
    public void init() {
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("offensive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Put offensive words into TrieNode
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                // Initialize begin node
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // Character already exist, go to the exist node
            tempNode = subNode;

            // set end sign
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * Offensive words Filter
     * @param text original text
     * @return filtered text
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        //Pointer 1
        TrieNode tempNode = rootNode;
        //Pointer 2
        int begin = 0;
        //Pointer 3
        int position = 0;
        //Result
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);
            // Skip symbol
            if (isSymbol(c)) {
                // if pointer1 is on the rootNode, go next node
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }
            // After skip all symbols, check next node
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // Store begin character
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // Discover offensive words, replace it
                sb.append(REPLACEMENT);
                //Go next node
                begin = ++position;
                tempNode = rootNode;
            } else {
                // Continue checking next character
                position++;
            }
        }
        // Store last few characters
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // Determine if it is a symbol
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private class TrieNode {
        // 关键词结束标识
        private boolean isKeywordEnd = false;

        //Key is the character, value is the node
        private Map<Character, TrieNode> subNodes = new HashMap<>();
        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // add sub node
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public  TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
