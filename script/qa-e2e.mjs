#!/usr/bin/env node
// 智能问答 E2E 测试
//
// 验证链路：登录 → 调 SSE 流式接口 → 校验「逐字/分块返回」+「语音播报音频事件」
//
// 用法（默认连本地网关，可用环境变量覆盖）：
//   node script/qa-e2e.mjs
//   BASE_URL=http://localhost:48080 USERNAME=admin PASSWORD=admin123 QUERY='你好' node script/qa-e2e.mjs

const BASE_URL = process.env.BASE_URL || 'http://localhost:48080'
const USERNAME = process.env.USERNAME || 'admin'
const PASSWORD = process.env.PASSWORD || 'admin123'
const QUERY = process.env.QUERY || '你好，请用一句话介绍你自己'

async function login() {
  const res = await fetch(`${BASE_URL}/admin-api/system/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: USERNAME, password: PASSWORD })
  })
  const json = await res.json()
  if (json.code !== 0) {
    throw new Error(`登录失败: ${JSON.stringify(json)}`)
  }
  return json.data.accessToken
}

// 解析 SSE 文本，返回 [{ event, data }]
function parseSse(text) {
  const events = []
  let event = 'message'
  let dataLines = []
  for (const line of text.split('\n')) {
    if (line.startsWith('event:')) {
      event = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    } else if (line === '' && dataLines.length > 0) {
      events.push({ event, data: dataLines.join('\n') })
      event = 'message'
      dataLines = []
    }
  }
  return events
}

async function run() {
  console.log(`[e2e] 目标: ${BASE_URL}`)
  console.log(`[e2e] 提问: ${QUERY}`)

  const token = await login()
  console.log('[e2e] 登录成功，获取 token')

  const uniqueRequestId = `e2e-${Date.now()}`
  const url = `${BASE_URL}/digital-api/system/workflow/getDigitalHumansStream?uniqueRequestId=${uniqueRequestId}`
  const res = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({
      query: QUERY,
      inputObj: {},
      type: '1',
      conversation_id: '',
      id: 10,
      uniqueRequestId
    })
  })
  if (!res.ok) {
    throw new Error(`SSE 接口返回 HTTP ${res.status}`)
  }

  const raw = await res.text()
  const events = parseSse(raw)
  const messages = events.filter((e) => e.event === 'message')
  const answer = messages.map((e) => e.data).join('')
  const audioEvents = events.filter((e) => e.event === 'audio')
  const audioUrl = audioEvents.length > 0 ? audioEvents[0].data : ''

  console.log(`[e2e] message 事件数: ${messages.length}`)
  console.log(`[e2e] 完整回答: ${answer}`)
  console.log(`[e2e] audio 事件数: ${audioEvents.length}`)
  console.log(`[e2e] audioUrl: ${audioUrl || '(无)'}`)

  const checks = [
    { name: '流式返回（收到 message 事件）', pass: messages.length > 0 },
    { name: '回答内容非空', pass: answer.trim().length > 0 },
    { name: '分块输出（message 事件 >= 2）', pass: messages.length >= 2 },
    { name: '语音播报（audio 事件返回 URL）', pass: !!audioUrl }
  ]

  let failed = false
  console.log('\n=== 结果 ===')
  for (const c of checks) {
    console.log(`${c.pass ? 'PASS' : 'FAIL'}  ${c.name}`)
    if (!c.pass) failed = true
  }
  process.exit(failed ? 1 : 0)
}

run().catch((err) => {
  console.error(`[e2e] 测试失败: ${err.message}`)
  process.exit(1)
})
