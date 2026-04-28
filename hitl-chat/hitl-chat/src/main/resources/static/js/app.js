/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const i18n = {
    en: {
        subtitle: 'Human-in-the-Loop Agent Demo',
        mcpServers: 'MCP Servers',
        addMcpServer: '+ Add MCP Server',
        availableTools: 'Available Tools',
        dangerousTools: 'Dangerous Tools',
        dangerousToolsHint: 'Check tools that require confirmation',
        save: 'Save',
        inputPlaceholder: 'Type a message...',
        send: 'Send',
        stop: 'Stop',
        addMcpServerTitle: 'Add MCP Server',
        name: 'Name',
        mcpNamePlaceholder: 'e.g. filesystem',
        transportType: 'Transport Type',
        stdioOption: 'StdIO (Local Process)',
        sseOption: 'SSE (HTTP Server-Sent Events)',
        httpOption: 'HTTP (Streamable HTTP)',
        command: 'Command',
        commandPlaceholder: 'e.g. npx',
        args: 'Arguments (comma separated)',
        argsPlaceholder: 'e.g. -y,@modelcontextprotocol/server-filesystem,/tmp',
        urlPlaceholder: 'e.g. https://mcp.example.com/sse',
        headers: 'Headers (JSON)',
        add: 'Add',
        cancel: 'Cancel',
        you: 'You',
        assistant: 'Assistant',
        noMcpServers: 'No MCP servers configured',
        noTools: 'No tools available',
        error: 'Error',
        toolCall: 'Tool Call',
        toolResult: 'Tool Result',
        dangerous: 'Dangerous',
        executing: 'Executing...',
        rejected: 'Rejected',
        confirm: '✓ Confirm',
        reject: '✗ Reject',
        params: 'Parameters',
        noOutput: '(no output)',
        confirmRemoveMcp: 'Remove MCP server "{name}"?',
        addMcpFailed: 'Failed to add MCP server',
        removeMcpFailed: 'Failed to remove MCP server',
        invalidHeadersJson: 'Invalid headers JSON format',
        dangerousToolsSaved: 'Dangerous tools configuration saved',
        saveDangerousToolsFailed: 'Failed to save dangerous tools configuration',
        adding: 'Adding...',
        chatError: 'Chat error',
        confirmError: 'Confirm error',
        interruptFailed: 'Interrupt request failed',
        userRejected: 'Rejected by user'
    },
    zh: {
        subtitle: '人机协作智能体演示',
        mcpServers: 'MCP 服务器',
        addMcpServer: '+ 添加 MCP 服务器',
        availableTools: '可用工具',
        dangerousTools: '高危工具',
        dangerousToolsHint: '勾选需要确认执行的工具',
        save: '保存',
        inputPlaceholder: '输入消息...',
        send: '发送',
        stop: '停止',
        addMcpServerTitle: '添加 MCP 服务器',
        name: '名称',
        mcpNamePlaceholder: '例如: filesystem',
        transportType: '传输类型',
        stdioOption: 'StdIO (本地进程)',
        sseOption: 'SSE (HTTP Server-Sent Events)',
        httpOption: 'HTTP (Streamable HTTP)',
        command: '命令',
        commandPlaceholder: '例如: npx',
        args: '参数 (逗号分隔)',
        argsPlaceholder: '例如: -y,@modelcontextprotocol/server-filesystem,/tmp',
        urlPlaceholder: '例如: https://mcp.example.com/sse',
        headers: '请求头 (JSON)',
        add: '添加',
        cancel: '取消',
        you: '你',
        assistant: '助手',
        noMcpServers: '暂无 MCP 服务器配置',
        noTools: '暂无可用工具',
        error: '错误',
        toolCall: '工具调用',
        toolResult: '工具结果',
        dangerous: '高危',
        executing: '执行中...',
        rejected: '已拒绝',
        confirm: '✓ 确认执行',
        reject: '✗ 拒绝',
        params: '参数',
        noOutput: '(无输出)',
        confirmRemoveMcp: '确定移除 MCP 服务器 "{name}"?',
        addMcpFailed: '添加 MCP 服务器失败',
        removeMcpFailed: '移除 MCP 服务器失败',
        invalidHeadersJson: '请求头 JSON 格式无效',
        dangerousToolsSaved: '高危工具配置已保存',
        saveDangerousToolsFailed: '保存高危工具配置失败',
        adding: '添加中...',
        chatError: '聊天错误',
        confirmError: '确认错误',
        interruptFailed: '中断请求失败',
        userRejected: '用户拒绝执行'
    }
};

const state = {
    sessionId: 'session_' + Date.now(),
    isProcessing: false,
    currentAssistantMessage: null,
    dangerousTools: new Set(),
    availableTools: new Set(),
    pendingToolCalls: null,
    currentAbortController: null,
    lang: 'en'
};

const elements = {
    chatMessages: document.getElementById('chat-messages'),
    messageInput: document.getElementById('message-input'),
    sendBtn: document.getElementById('send-btn'),
    stopBtn: document.getElementById('stop-btn'),
    mcpList: document.getElementById('mcp-list'),
    toolsList: document.getElementById('tools-list'),
    dangerousToolsList: document.getElementById('dangerous-tools-list'),
    addMcpBtn: document.getElementById('add-mcp-btn'),
    mcpModal: document.getElementById('mcp-modal'),
    mcpForm: document.getElementById('mcp-form'),
    cancelMcpBtn: document.getElementById('cancel-mcp-btn'),
    mcpTransport: document.getElementById('mcp-transport'),
    stdioOptions: document.getElementById('stdio-options'),
    httpOptions: document.getElementById('http-options'),
    saveDangerousToolsBtn: document.getElementById('save-dangerous-tools-btn'),
    langEn: document.getElementById('lang-en'),
    langZh: document.getElementById('lang-zh')
};

function t(key) {
    return i18n[state.lang][key] || key;
}

function updateI18n() {
    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        el.textContent = t(key);
    });
    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
        const key = el.getAttribute('data-i18n-placeholder');
        el.placeholder = t(key);
    });
    document.querySelectorAll('select option[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        el.textContent = t(key);
    });
    renderMcpList(window._mcpServers || []);
    renderToolsList(Array.from(state.availableTools));
    renderDangerousToolsList();
}

function setLanguage(lang) {
    state.lang = lang;
    elements.langEn.classList.toggle('active', lang === 'en');
    elements.langZh.classList.toggle('active', lang === 'zh');
    updateI18n();
}

document.addEventListener('DOMContentLoaded', () => {
    loadMcpServers();
    loadTools();
    loadDangerousTools();
    setupEventListeners();
    updateI18n();
});

function setupEventListeners() {
    elements.sendBtn.addEventListener('click', sendMessage);
    elements.messageInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
    elements.stopBtn.addEventListener('click', stopGeneration);
    elements.addMcpBtn.addEventListener('click', () => showModal(elements.mcpModal));
    elements.cancelMcpBtn.addEventListener('click', () => hideModal(elements.mcpModal));
    elements.mcpForm.addEventListener('submit', addMcpServer);
    elements.mcpTransport.addEventListener('change', updateTransportOptions);
    elements.saveDangerousToolsBtn.addEventListener('click', saveDangerousTools);
    elements.langEn.addEventListener('click', () => setLanguage('en'));
    elements.langZh.addEventListener('click', () => setLanguage('zh'));
}

async function sendMessage() {
    const message = elements.messageInput.value.trim();
    if (!message || state.isProcessing) return;

    elements.messageInput.value = '';
    addMessage('user', message);
    setProcessing(true);
    state.currentAssistantMessage = null;

    state.currentAbortController = new AbortController();

    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionId: state.sessionId, message }),
            signal: state.currentAbortController.signal
        });
        await processSSEStream(response);
    } catch (error) {
        if (error.name !== 'AbortError') {
            console.error(t('chatError') + ':', error);
            addMessage('assistant', t('error') + ': ' + error.message);
        }
    } finally {
        setProcessing(false);
        state.currentAbortController = null;
    }
}

async function stopGeneration() {
    try {
        await fetch(`/api/chat/interrupt/${encodeURIComponent(state.sessionId)}`, {
            method: 'POST'
        });
    } catch (error) {
        console.error(t('interruptFailed') + ':', error);
    }
}

async function processSSEStream(response) {
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    try {
        while (true) {
            const { done, value } = await reader.read();
            if (value) buffer += decoder.decode(value, { stream: true });

            const lines = buffer.split('\n');
            buffer = lines.pop() || '';

            for (const line of lines) {
                if (line.startsWith('data:')) {
                    const data = line.slice(5).trim();
                    if (data) {
                        try {
                            handleChatEvent(JSON.parse(data));
                        } catch (e) {
                            console.error('Failed:', data, e);
                        }
                    }
                }
            }
            if (done) break;
        }
    } finally {
        reader.releaseLock();
    }
}

function handleChatEvent(event) {
    switch (event.type) {
        case 'TEXT':
            if (event.incremental) {
                appendToAssistantMessage(event.content);
            } else {
                finalizeAssistantMessage();
            }
            break;
        case 'TOOL_USE':
            finalizeAssistantMessage();
            addToolUseEvent(event.toolId, event.toolName, event.toolInput, false);
            break;
        case 'TOOL_RESULT':
            addToolResultEvent(event.toolId, event.toolName, event.toolResult);
            break;
        case 'TOOL_CONFIRM':
            finalizeAssistantMessage();
            state.pendingToolCalls = event.pendingToolCalls;
            showInlineToolConfirmation(event.pendingToolCalls);
            break;
        case 'ERROR':
            finalizeAssistantMessage();
            addMessage('assistant', t('error') + ': ' + event.error);
            break;
        case 'COMPLETE':
            finalizeAssistantMessage();
            break;
    }
}

function addMessage(role, content) {
    const div = document.createElement('div');
    div.className = `message ${role}`;
    div.innerHTML = `
        <div class="message-label">${role === 'user' ? t('you') : t('assistant')}</div>
        <div class="message-content">${escapeHtml(content)}</div>
    `;
    elements.chatMessages.appendChild(div);
    scrollToBottom();
    if (role === 'assistant') {
        state.currentAssistantMessage = div.querySelector('.message-content');
    }
}

function appendToAssistantMessage(content) {
    if (!state.currentAssistantMessage) {
        addMessage('assistant', content);
    } else {
        state.currentAssistantMessage.textContent += content;
        scrollToBottom();
    }
}

function finalizeAssistantMessage() {
    state.currentAssistantMessage = null;
}

function addToolUseEvent(toolId, toolName, input, needsConfirm) {
    const div = document.createElement('div');
    const isDangerous = state.dangerousTools.has(toolName);
    div.className = `tool-event ${isDangerous ? 'dangerous' : ''}`;
    div.id = `tool-${toolId}`;

    const inputJson = input ? JSON.stringify(input, null, 2) : '{}';
    div.innerHTML = `
        <div class="tool-event-header">
            <span class="icon">🔧</span>
            <span>${t('toolCall')}: <code>${escapeHtml(toolName)}</code></span>
            ${isDangerous ? `<span class="badge-danger">${t('dangerous')}</span>` : ''}
        </div>
        <div class="tool-params">
            <div class="params-label">${t('params')}:</div>
            <pre>${escapeHtml(inputJson)}</pre>
        </div>
        ${needsConfirm ? `
        <div class="tool-confirm-actions" id="confirm-actions-${toolId}">
            <button class="btn btn-sm btn-primary" onclick="confirmToolCall('${toolId}', true)">${t('confirm')}</button>
            <button class="btn btn-sm btn-secondary" onclick="confirmToolCall('${toolId}', false)">${t('reject')}</button>
        </div>
        ` : `<div class="tool-status executing">${t('executing')}</div>`}
    `;
    elements.chatMessages.appendChild(div);
    scrollToBottom();
}

function showInlineToolConfirmation(pendingToolCalls) {
    for (const tool of pendingToolCalls) {
        addToolUseEvent(tool.id, tool.name, tool.input, true);
    }
}

async function confirmToolCall(toolId, confirmed) {
    const actionsDiv = document.getElementById(`confirm-actions-${toolId}`);
    if (actionsDiv) {
        actionsDiv.innerHTML = confirmed
            ? `<div class="tool-status executing">${t('executing')}</div>`
            : `<div class="tool-status rejected">${t('rejected')}</div>`;
    }

    if (!confirmed) {
        for (const tool of state.pendingToolCalls) {
            if (tool.id !== toolId) {
                const otherActions = document.getElementById(`confirm-actions-${tool.id}`);
                if (otherActions) {
                    otherActions.innerHTML = `<div class="tool-status rejected">${t('rejected')}</div>`;
                }
            }
        }
    }

    const toolCalls = state.pendingToolCalls.map(t => ({ id: t.id, name: t.name }));
    state.pendingToolCalls = null;

    setProcessing(true);
    state.currentAbortController = new AbortController();

    try {
        const response = await fetch('/api/chat/confirm', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                sessionId: state.sessionId,
                confirmed,
                reason: confirmed ? null : t('userRejected'),
                toolCalls
            }),
            signal: state.currentAbortController.signal
        });
        await processSSEStream(response);
    } catch (error) {
        if (error.name !== 'AbortError') {
            console.error(t('confirmError') + ':', error);
            addMessage('assistant', t('error') + ': ' + error.message);
        }
    } finally {
        setProcessing(false);
        state.currentAbortController = null;
    }
}

function addToolResultEvent(toolId, toolName, result) {
    const toolDiv = document.getElementById(`tool-${toolId}`);
    if (toolDiv) {
        const statusDiv = toolDiv.querySelector('.tool-status');
        if (statusDiv) {
            statusDiv.remove();
        }
    }

    const div = document.createElement('div');
    div.className = 'tool-event result';
    div.innerHTML = `
        <div class="tool-event-header">
            <span class="icon">✅</span>
            <span>${t('toolResult')}: <code>${escapeHtml(toolName)}</code></span>
        </div>
        <div class="tool-result-content">
            <pre>${escapeHtml(result || t('noOutput'))}</pre>
        </div>
    `;
    elements.chatMessages.appendChild(div);
    scrollToBottom();
}

async function loadMcpServers() {
    try {
        const response = await fetch('/api/mcp/list');
        const servers = await response.json();
        window._mcpServers = servers;
        renderMcpList(servers);
    } catch (error) {
        console.error('Load MCP servers failed:', error);
    }
}

function renderMcpList(servers) {
    window._mcpServers = servers;
    if (servers.length === 0) {
        elements.mcpList.innerHTML = `<p class="hint">${t('noMcpServers')}</p>`;
        return;
    }
    elements.mcpList.innerHTML = servers.map(name => `
        <div class="mcp-item">
            <span>${escapeHtml(name)}</span>
            <button class="btn-remove" onclick="removeMcpServer('${escapeHtml(name)}')">&times;</button>
        </div>
    `).join('');
}

async function addMcpServer(e) {
    e.preventDefault();

    const submitBtn = elements.mcpForm.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span class="spinner"></span> ${t('adding')}`;

    const name = document.getElementById('mcp-name').value;
    const transportType = elements.mcpTransport.value;
    const request = { name, transportType };

    if (transportType === 'STDIO') {
        request.command = document.getElementById('mcp-command').value;
        const argsValue = document.getElementById('mcp-args').value;
        request.args = argsValue ? argsValue.split(',').map(s => s.trim()) : [];
    } else {
        request.url = document.getElementById('mcp-url').value;
        const headersValue = document.getElementById('mcp-headers').value;
        if (headersValue) {
            try {
                request.headers = JSON.parse(headersValue);
            } catch (e) {
                alert(t('invalidHeadersJson'));
                submitBtn.disabled = false;
                submitBtn.textContent = originalText;
                return;
            }
        }
    }

    try {
        const response = await fetch('/api/mcp/add', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request)
        });
        const result = await response.json();

        if (result.success) {
            hideModal(elements.mcpModal);
            elements.mcpForm.reset();
            loadMcpServers();
            loadTools();
        } else {
            alert(t('addMcpFailed') + ': ' + result.error);
        }
    } catch (error) {
        alert(t('error') + ': ' + error.message);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }
}

async function removeMcpServer(name) {
    if (!confirm(t('confirmRemoveMcp').replace('{name}', name))) return;
    try {
        const response = await fetch(`/api/mcp/${encodeURIComponent(name)}`, { method: 'DELETE' });
        const result = await response.json();
        if (result.success) {
            loadMcpServers();
            loadTools();
        } else {
            alert(t('removeMcpFailed') + ': ' + result.error);
        }
    } catch (error) {
        alert(t('error') + ': ' + error.message);
    }
}

function updateTransportOptions() {
    const transport = elements.mcpTransport.value;
    elements.stdioOptions.classList.toggle('hidden', transport !== 'STDIO');
    elements.httpOptions.classList.toggle('hidden', transport === 'STDIO');
}

async function loadTools() {
    try {
        const response = await fetch('/api/tools');
        const tools = await response.json();
        state.availableTools = new Set(tools);
        renderToolsList(tools);
        renderDangerousToolsList();
    } catch (error) {
        console.error('Load tools failed:', error);
    }
}

function renderToolsList(tools) {
    if (tools.length === 0) {
        elements.toolsList.innerHTML = `<p class="hint">${t('noTools')}</p>`;
        return;
    }
    elements.toolsList.innerHTML = tools.map(name => `
        <div class="tool-item">${escapeHtml(name)}</div>
    `).join('');
}

async function loadDangerousTools() {
    try {
        const response = await fetch('/api/settings/dangerous-tools');
        const tools = await response.json();
        state.dangerousTools = new Set(tools);
        renderDangerousToolsList();
    } catch (error) {
        console.error('Load dangerous tools failed:', error);
    }
}

function renderDangerousToolsList() {
    const tools = Array.from(state.availableTools);
    if (tools.length === 0) {
        elements.dangerousToolsList.innerHTML = `<p class="hint">${t('noTools')}</p>`;
        return;
    }
    elements.dangerousToolsList.innerHTML = tools.map(name => {
        const isDangerous = state.dangerousTools.has(name);
        return `
            <div class="dangerous-tool-item ${isDangerous ? 'is-dangerous' : ''}">
                <input type="checkbox" id="dangerous-${escapeHtml(name)}" 
                       ${isDangerous ? 'checked' : ''} 
                       onchange="toggleDangerousTool('${escapeHtml(name)}', this.checked)">
                <label for="dangerous-${escapeHtml(name)}">${escapeHtml(name)}</label>
            </div>
        `;
    }).join('');
}

function toggleDangerousTool(name, isDangerous) {
    if (isDangerous) {
        state.dangerousTools.add(name);
    } else {
        state.dangerousTools.delete(name);
    }
    renderDangerousToolsList();
}

async function saveDangerousTools() {
    try {
        const response = await fetch('/api/settings/dangerous-tools', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(Array.from(state.dangerousTools))
        });
        const result = await response.json();
        if (result.success) {
            alert(t('dangerousToolsSaved'));
        } else {
            alert(t('saveDangerousToolsFailed'));
        }
    } catch (error) {
        alert(t('error') + ': ' + error.message);
    }
}

function setProcessing(processing) {
    state.isProcessing = processing;
    elements.sendBtn.disabled = processing;
    elements.messageInput.disabled = processing;
    elements.stopBtn.classList.toggle('hidden', !processing);
    elements.sendBtn.classList.toggle('hidden', processing);
}

function showModal(modal) {
    modal.classList.remove('hidden');
}

function hideModal(modal) {
    modal.classList.add('hidden');
}

function scrollToBottom() {
    elements.chatMessages.scrollTop = elements.chatMessages.scrollHeight;
}

function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(text);
    return div.innerHTML;
}