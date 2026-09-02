import { useState, useRef, useEffect, useCallback } from 'react';
import { MessageCircle, X, Send, Bus, MapPin, Route, Clock, HelpCircle, Loader2 } from 'lucide-react';
import { sendChatMessage } from '../services/api';

/* ───────────────────────── simple markdown renderer ────────────── */

function renderMarkdown(text) {
  return text.split('\n').map((line, i) => {
    // Bold
    const parts = [];
    let remaining = line;
    let key = 0;
    while (remaining.includes('**')) {
      const start = remaining.indexOf('**');
      const end = remaining.indexOf('**', start + 2);
      if (end === -1) break;
      if (start > 0) parts.push(<span key={key++}>{remaining.slice(0, start)}</span>);
      parts.push(<strong key={key++}>{remaining.slice(start + 2, end)}</strong>);
      remaining = remaining.slice(end + 2);
    }
    if (remaining) parts.push(<span key={key++}>{remaining}</span>);
    return (
      <span key={i}>
        {parts.length ? parts : line}
        {i < text.split('\n').length - 1 && <br />}
      </span>
    );
  });
}

/* ───────────────────────── ChatBot component ──────────────────── */

const ChatBot = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [hasOpened, setHasOpened] = useState(false);
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => { scrollToBottom(); }, [messages, isTyping, scrollToBottom]);

  useEffect(() => {
    if (isOpen && inputRef.current) inputRef.current.focus();
  }, [isOpen]);

  const openChat = () => {
    setIsOpen(true);
    if (!hasOpened) {
      setHasOpened(true);
      setMessages([{
        id: Date.now(),
        sender: 'bot',
        text: "Hello! 👋 I'm the **BusSetu Assistant**.\n\nI can help you find bus routes, stops, and live tracking info.\n\nType **help** to see all available commands!",
        icon: 'bus',
        time: new Date(),
      }]);
    }
  };

  const sendMessage = async () => {
    const trimmed = input.trim();
    if (!trimmed) return;

    const userMsg = { id: Date.now(), sender: 'user', text: trimmed, time: new Date() };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setIsTyping(true);

    try {
      const res = await sendChatMessage({ message: trimmed });
      const response = res.data;
      setMessages(prev => [...prev, {
        id: Date.now() + 1,
        sender: 'bot',
        text: response.text,
        icon: response.icon,
        time: new Date(),
      }]);
    } catch (err) {
      setMessages(prev => [...prev, {
        id: Date.now() + 1,
        sender: 'bot',
        text: '⚠️ Sorry, I had trouble fetching that data. Please make sure the backend server is running and try again.',
        icon: 'help',
        time: new Date(),
      }]);
    } finally {
      setIsTyping(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const getIconComponent = (icon) => {
    const style = { width: 16, height: 16 };
    switch (icon) {
      case 'bus': return <Bus style={style} />;
      case 'pin': return <MapPin style={style} />;
      case 'route': return <Route style={style} />;
      case 'clock': return <Clock style={style} />;
      default: return <HelpCircle style={style} />;
    }
  };

  const formatTime = (date) => {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const quickActions = [
    { label: '🚌 Routes', msg: 'routes' },
    { label: '🟢 Active Buses', msg: 'active buses' },
    { label: '❓ Help', msg: 'help' },
  ];

  return (
    <>
      {/* ── FAB Button ── */}
      {!isOpen && (
        <button
          id="chatbot-fab"
          onClick={openChat}
          style={{
            position: 'fixed',
            bottom: '24px',
            right: '24px',
            width: '60px',
            height: '60px',
            borderRadius: '50%',
            background: 'linear-gradient(135deg, #2563eb 0%, #0d9488 100%)',
            border: 'none',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 8px 32px rgba(37,99,235,0.4), 0 0 0 0 rgba(37,99,235,0.3)',
            zIndex: 9999,
            transition: 'transform 0.3s, box-shadow 0.3s',
          }}
          className="chatbot-fab-pulse"
          onMouseEnter={e => { e.currentTarget.style.transform = 'scale(1.1)'; e.currentTarget.style.boxShadow = '0 12px 40px rgba(37,99,235,0.5)'; }}
          onMouseLeave={e => { e.currentTarget.style.transform = 'scale(1)'; e.currentTarget.style.boxShadow = '0 8px 32px rgba(37,99,235,0.4)'; }}
        >
          <MessageCircle style={{ width: 28, height: 28, color: 'white' }} />
        </button>
      )}

      {/* ── Chat Window ── */}
      {isOpen && (
        <div
          id="chatbot-window"
          className="chatbot-slide-up"
          style={{
            position: 'fixed',
            bottom: '24px',
            right: '24px',
            width: '400px',
            maxWidth: 'calc(100vw - 32px)',
            height: '560px',
            maxHeight: 'calc(100vh - 48px)',
            background: '#ffffff',
            borderRadius: '24px',
            boxShadow: '0 25px 60px rgba(0,0,0,0.15), 0 0 0 1px rgba(0,0,0,0.05)',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
            zIndex: 9999,
          }}
        >
          {/* Header */}
          <div style={{
            background: 'linear-gradient(135deg, #1d4ed8 0%, #0d9488 100%)',
            padding: '18px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
          }}>
            <div style={{
              width: '40px', height: '40px', borderRadius: '12px',
              background: 'rgba(255,255,255,0.2)', backdropFilter: 'blur(10px)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              border: '1px solid rgba(255,255,255,0.25)',
            }}>
              <Bus style={{ width: 22, height: 22, color: 'white' }} />
            </div>
            <div style={{ flex: 1 }}>
              <p style={{ color: 'white', fontWeight: 700, fontSize: '15px', margin: 0 }}>BusSetu Assistant</p>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '2px' }}>
                <span style={{ width: '7px', height: '7px', borderRadius: '50%', background: '#34d399', display: 'inline-block', boxShadow: '0 0 0 2px rgba(52,211,153,0.3)' }} />
                <span style={{ color: 'rgba(255,255,255,0.7)', fontSize: '11px', fontWeight: 500 }}>Online · Instant replies</span>
              </div>
            </div>
            <button
              id="chatbot-close"
              onClick={() => setIsOpen(false)}
              style={{
                width: '32px', height: '32px', borderRadius: '10px',
                background: 'rgba(255,255,255,0.15)', border: 'none',
                cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
                transition: 'background 0.2s',
              }}
              onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.25)'}
              onMouseLeave={e => e.currentTarget.style.background = 'rgba(255,255,255,0.15)'}
            >
              <X style={{ width: 18, height: 18, color: 'white' }} />
            </button>
          </div>

          {/* Messages */}
          <div style={{
            flex: 1, overflowY: 'auto', padding: '16px',
            display: 'flex', flexDirection: 'column', gap: '12px',
            background: '#f8fafc',
          }} className="chatbot-messages-scroll">
            {messages.map(msg => (
              <div
                key={msg.id}
                className="chatbot-message-in"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: msg.sender === 'user' ? 'flex-end' : 'flex-start',
                }}
              >
                <div style={{
                  maxWidth: '85%',
                  padding: '12px 16px',
                  borderRadius: msg.sender === 'user'
                    ? '18px 18px 4px 18px'
                    : '18px 18px 18px 4px',
                  background: msg.sender === 'user'
                    ? 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)'
                    : 'white',
                  color: msg.sender === 'user' ? 'white' : '#1e293b',
                  fontSize: '14px',
                  lineHeight: 1.5,
                  boxShadow: msg.sender === 'user'
                    ? '0 2px 8px rgba(37,99,235,0.2)'
                    : '0 1px 4px rgba(0,0,0,0.06)',
                  border: msg.sender === 'user' ? 'none' : '1px solid #f1f5f9',
                }}>
                  {msg.sender === 'bot' && msg.icon && (
                    <div style={{
                      display: 'inline-flex', alignItems: 'center', gap: '6px',
                      color: '#2563eb', fontSize: '11px', fontWeight: 600,
                      marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '0.05em',
                    }}>
                      {getIconComponent(msg.icon)}
                      <span>BusSetu</span>
                    </div>
                  )}
                  <div>{msg.sender === 'bot' ? renderMarkdown(msg.text) : msg.text}</div>
                </div>
                <span style={{
                  fontSize: '10px', color: '#94a3b8', marginTop: '4px',
                  paddingLeft: msg.sender === 'user' ? 0 : '8px',
                  paddingRight: msg.sender === 'user' ? '8px' : 0,
                }}>
                  {formatTime(msg.time)}
                </span>
              </div>
            ))}

            {/* Typing indicator */}
            {isTyping && (
              <div style={{ display: 'flex', alignItems: 'flex-start' }}>
                <div style={{
                  background: 'white', border: '1px solid #f1f5f9',
                  borderRadius: '18px 18px 18px 4px', padding: '14px 20px',
                  boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
                  display: 'flex', alignItems: 'center', gap: '5px',
                }}>
                  <div className="chatbot-typing-dot" style={{ animationDelay: '0ms' }} />
                  <div className="chatbot-typing-dot" style={{ animationDelay: '150ms' }} />
                  <div className="chatbot-typing-dot" style={{ animationDelay: '300ms' }} />
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Quick actions (only show when few messages) */}
          {messages.length <= 1 && (
            <div style={{
              padding: '0 16px 8px', display: 'flex', gap: '8px',
              background: '#f8fafc',
            }}>
              {quickActions.map(qa => (
                <button
                  key={qa.msg}
                  onClick={() => { setInput(qa.msg); setTimeout(() => { setInput(qa.msg); sendMessage(); }, 50); }}
                  onMouseDown={(e) => {
                    e.preventDefault();
                    const userMsg = { id: Date.now(), sender: 'user', text: qa.msg, time: new Date() };
                    setMessages(prev => [...prev, userMsg]);
                    setIsTyping(true);
                    sendChatMessage({ message: qa.msg }).then(res => {
                      const response = res.data;
                      setMessages(prev => [...prev, {
                        id: Date.now() + 1, sender: 'bot',
                        text: response.text, icon: response.icon, time: new Date(),
                      }]);
                    }).catch(() => {
                      setMessages(prev => [...prev, {
                        id: Date.now() + 1, sender: 'bot',
                        text: '⚠️ Could not fetch data. Is the backend running?',
                        icon: 'help', time: new Date(),
                      }]);
                    }).finally(() => setIsTyping(false));
                  }}
                  style={{
                    flex: 1, padding: '8px 12px', borderRadius: '12px',
                    border: '1px solid #e2e8f0', background: 'white',
                    fontSize: '12px', fontWeight: 600, color: '#475569',
                    cursor: 'pointer', transition: 'all 0.2s',
                  }}
                  onMouseEnter={e => { e.currentTarget.style.borderColor = '#2563eb'; e.currentTarget.style.color = '#2563eb'; e.currentTarget.style.background = '#eff6ff'; }}
                  onMouseLeave={e => { e.currentTarget.style.borderColor = '#e2e8f0'; e.currentTarget.style.color = '#475569'; e.currentTarget.style.background = 'white'; }}
                >
                  {qa.label}
                </button>
              ))}
            </div>
          )}

          {/* Input */}
          <div style={{
            padding: '12px 16px', borderTop: '1px solid #f1f5f9',
            display: 'flex', alignItems: 'center', gap: '10px',
            background: 'white',
          }}>
            <input
              ref={inputRef}
              id="chatbot-input"
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask about routes, stops, buses..."
              style={{
                flex: 1, border: '1px solid #e2e8f0', borderRadius: '14px',
                padding: '12px 16px', fontSize: '14px', outline: 'none',
                transition: 'border-color 0.2s', background: '#f8fafc',
                fontFamily: "'Inter', sans-serif",
              }}
              onFocus={e => e.target.style.borderColor = '#2563eb'}
              onBlur={e => e.target.style.borderColor = '#e2e8f0'}
            />
            <button
              id="chatbot-send"
              onClick={sendMessage}
              disabled={!input.trim() || isTyping}
              style={{
                width: '44px', height: '44px', borderRadius: '14px',
                background: input.trim() && !isTyping
                  ? 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)'
                  : '#e2e8f0',
                border: 'none', cursor: input.trim() && !isTyping ? 'pointer' : 'not-allowed',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                transition: 'all 0.2s', flexShrink: 0,
              }}
            >
              {isTyping
                ? <Loader2 style={{ width: 20, height: 20, color: '#94a3b8', animation: 'spin 1s linear infinite' }} />
                : <Send style={{ width: 20, height: 20, color: input.trim() ? 'white' : '#94a3b8' }} />
              }
            </button>
          </div>
        </div>
      )}
    </>
  );
};

export default ChatBot;
