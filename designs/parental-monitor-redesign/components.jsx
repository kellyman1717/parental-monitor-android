// components.jsx — Shared reusable UI components for Parental Monitor redesign
// All components accept a `p` prop (palette tokens) and `TYPE`, `SPACE`, `RADIUS` globals.

// ─── Card wrapper ───────────────────────────────────────────
function Card({ children, p, style = {}, padding, elevation = false, onClick }) {
  return (
    <div onClick={onClick} style={{
      background: p.surface,
      borderRadius: '14px',
      border: `1px solid ${p.border}`,
      padding: padding || '16px',
      boxShadow: elevation ? `0 4px 20px ${p.shadow}` : 'none',
      cursor: onClick ? 'pointer' : 'default',
      transition: 'box-shadow 0.15s, transform 0.15s',
      ...style,
    }}>
      {children}
    </div>
  );
}

// ─── Section title ──────────────────────────────────────────
function SectionTitle({ children, p, action, onAction }) {
  return (
    <div style={{
      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
      padding: '4px 0 8px',
    }}>
      <span style={{
        fontSize: '15px', fontWeight: 600, color: p.text,
        fontFamily: TYPE.family,
      }}>{children}</span>
      {action && (
        <span onClick={onAction} style={{
          fontSize: '13px', fontWeight: 500, color: p.primary,
          cursor: 'pointer', fontFamily: TYPE.family,
        }}>{action}</span>
      )}
    </div>
  );
}

// ─── Stat tile (small card with number + label) ─────────────
function StatTile({ value, label, icon, p, accentColor }) {
  const color = accentColor || p.primary;
  return (
    <div style={{
      flex: '1 1 0', minWidth: 0,
      background: p.surface, borderRadius: '12px',
      border: `1px solid ${p.border}`,
      padding: '14px 12px', textAlign: 'center',
      display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '6px',
    }}>
      <div style={{
        width: 36, height: 36, borderRadius: '10px',
        background: color + '18', display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        {icon && React.cloneElement(icon, { size: 18, color })}
      </div>
      <span style={{
        fontSize: '22px', fontWeight: 700, color, fontFamily: TYPE.family,
        lineHeight: 1,
      }}>{value}</span>
      <span style={{
        fontSize: '11px', fontWeight: 500, color: p.textMuted,
        fontFamily: TYPE.family,
      }}>{label}</span>
    </div>
  );
}

// ─── Status pill ────────────────────────────────────────────
function StatusPill({ label, color, p }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: '4px',
      padding: '4px 10px', borderRadius: '999px',
      background: color + '18', color, fontSize: '12px', fontWeight: 600,
      fontFamily: TYPE.family,
    }}>
      <span style={{
        width: 6, height: 6, borderRadius: '50%', background: color,
      }} />
      {label}
    </span>
  );
}

// ─── Progress bar ───────────────────────────────────────────
function ProgressBar({ value, max = 1, color, p, height = 8 }) {
  const pct = Math.min(100, Math.max(0, (value / max) * 100));
  return (
    <div style={{
      width: '100%', height, borderRadius: height / 2,
      background: p.border, overflow: 'hidden',
    }}>
      <div style={{
        width: pct + '%', height: '100%', borderRadius: height / 2,
        background: color || p.primary,
        transition: 'width 0.4s ease',
      }} />
    </div>
  );
}

// ─── App icon placeholder ───────────────────────────────────
function AppIcon({ name, color, size = 40, p }) {
  const initial = (name || '?')[0].toUpperCase();
  return (
    <div style={{
      width: size, height: size, borderRadius: size * 0.28,
      background: (color || p.primary) + '15',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontSize: size * 0.42, fontWeight: 700, color: color || p.primary,
      fontFamily: TYPE.family, flexShrink: 0,
    }}>
      {initial}
    </div>
  );
}

// ─── List item row ──────────────────────────────────────────
function ListItem({ leading, title, subtitle, trailing, p, onClick, dense }) {
  return (
    <div onClick={onClick} style={{
      display: 'flex', alignItems: 'center', gap: '12px',
      padding: dense ? '10px 0' : '14px 0',
      borderBottom: `1px solid ${p.border}`,
      cursor: onClick ? 'pointer' : 'default',
      fontFamily: TYPE.family,
    }}>
      {leading}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{
          fontSize: '14px', fontWeight: 500, color: p.text,
          whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
        }}>{title}</div>
        {subtitle && (
          <div style={{
            fontSize: '12px', color: p.textMuted, marginTop: '2px',
            whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
          }}>{subtitle}</div>
        )}
      </div>
      {trailing && (
        <div style={{ flexShrink: 0, textAlign: 'right' }}>{trailing}</div>
      )}
    </div>
  );
}

// ─── Tab switcher ───────────────────────────────────────────
function TabSwitcher({ tabs, active, onChange, p }) {
  return (
    <div style={{
      display: 'flex', background: p.surfaceAlt, borderRadius: '10px',
      padding: '3px', gap: '2px',
    }}>
      {tabs.map((tab, i) => (
        <div key={i} onClick={() => onChange(i)} style={{
          flex: 1, textAlign: 'center', padding: '9px 8px',
          borderRadius: '8px', fontSize: '13px', fontWeight: 500,
          fontFamily: TYPE.family, cursor: 'pointer',
          transition: 'all 0.15s',
          background: active === i ? p.surface : 'transparent',
          color: active === i ? p.primary : p.textMuted,
          boxShadow: active === i ? `0 1px 4px ${p.shadow}` : 'none',
        }}>
          {tab}
        </div>
      ))}
    </div>
  );
}

// ─── Empty state ────────────────────────────────────────────
function EmptyState({ icon, title, subtitle, p }) {
  return (
    <div style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      justifyContent: 'center', padding: '48px 24px', gap: '12px',
      fontFamily: TYPE.family,
    }}>
      <div style={{ opacity: 0.4 }}>{icon}</div>
      <div style={{ fontSize: '15px', fontWeight: 500, color: p.textMuted }}>{title}</div>
      {subtitle && <div style={{ fontSize: '13px', color: p.textSubtle }}>{subtitle}</div>}
    </div>
  );
}

// ─── Button ─────────────────────────────────────────────────
function Btn({ children, p, variant = 'primary', fullWidth, onClick, disabled, style = {} }) {
  const isFilled = variant === 'primary';
  const isGhost = variant === 'ghost';
  return (
    <button onClick={onClick} disabled={disabled} style={{
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
      gap: '8px', padding: '12px 20px', borderRadius: '10px',
      fontSize: '14px', fontWeight: 600, fontFamily: TYPE.family,
      cursor: disabled ? 'not-allowed' : 'pointer',
      opacity: disabled ? 0.5 : 1,
      border: isFilled ? 'none' : `1.5px solid ${p.border}`,
      background: isFilled ? p.primary : isGhost ? 'transparent' : p.surface,
      color: isFilled ? p.onPrimary : p.text,
      width: fullWidth ? '100%' : 'auto',
      transition: 'all 0.15s',
      ...style,
    }}>
      {children}
    </button>
  );
}

// ─── Input field ────────────────────────────────────────────
function InputField({ label, value, onChange, placeholder, type = 'text', prefix, suffix, p }) {
  const [focused, setFocused] = React.useState(false);
  return (
    <div style={{ fontFamily: TYPE.family }}>
      {label && (
        <label style={{
          display: 'block', fontSize: '13px', fontWeight: 500,
          color: p.textMuted, marginBottom: '6px',
        }}>{label}</label>
      )}
      <div style={{
        display: 'flex', alignItems: 'center', gap: '8px',
        border: `1.5px solid ${focused ? p.primary : p.border}`,
        borderRadius: '10px', padding: '10px 14px',
        background: p.surface, transition: 'border-color 0.15s',
      }}>
        {prefix && <span style={{ fontSize: '13px', color: p.textSubtle, flexShrink: 0 }}>{prefix}</span>}
        <input
          type={type}
          value={value}
          onChange={e => onChange(e.target.value)}
          placeholder={placeholder}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          style={{
            flex: 1, border: 'none', outline: 'none', background: 'transparent',
            fontSize: '14px', color: p.text, fontFamily: TYPE.family,
          }}
        />
        {suffix && <span style={{ fontSize: '13px', color: p.textSubtle, flexShrink: 0 }}>{suffix}</span>}
      </div>
    </div>
  );
}

// ─── Switch toggle ──────────────────────────────────────────
function Switch({ checked, onChange, p }) {
  return (
    <div onClick={() => onChange(!checked)} style={{
      width: 48, height: 28, borderRadius: 14,
      background: checked ? p.primary : p.border,
      padding: 3, cursor: 'pointer', transition: 'background 0.2s',
      display: 'flex', alignItems: checked ? 'center' : 'center',
      justifyContent: checked ? 'flex-end' : 'flex-start',
    }}>
      <div style={{
        width: 22, height: 22, borderRadius: 11,
        background: '#fff', boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
        transition: 'transform 0.2s',
      }} />
    </div>
  );
}

// ─── Top bar ────────────────────────────────────────────────
function TopBar({ title, p, onBack, dark = true, right }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: '8px',
      padding: '10px 8px 10px 4px',
      background: dark ? p.primary : p.surface,
      color: dark ? p.onPrimary : p.text,
      minHeight: 52,
    }}>
      {onBack && (
        <div onClick={onBack} style={{
          width: 44, height: 44, display: 'flex', alignItems: 'center',
          justifyContent: 'center', cursor: 'pointer', borderRadius: '50%',
        }}>
          <IconArrowLeft size={22} color={dark ? '#fff' : p.text} />
        </div>
      )}
      <span style={{
        flex: 1, fontSize: '18px', fontWeight: 600, fontFamily: TYPE.family,
        color: dark ? '#fff' : p.text,
      }}>{title}</span>
      {right}
    </div>
  );
}

// ─── Bottom nav ─────────────────────────────────────────────
function BottomNav({ items, active, onChange, p }) {
  return (
    <div style={{
      display: 'flex', borderTop: `1px solid ${p.border}`,
      background: p.surface, padding: '6px 0 8px',
    }}>
      {items.map((item, i) => (
        <div key={i} onClick={() => onChange(i)} style={{
          flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center',
          gap: '3px', cursor: 'pointer', padding: '4px 0',
        }}>
          {React.cloneElement(item.icon, {
            size: 22, color: active === i ? p.primary : p.textSubtle,
          })}
          <span style={{
            fontSize: '10px', fontWeight: active === i ? 600 : 400,
            color: active === i ? p.primary : p.textSubtle,
            fontFamily: TYPE.family,
          }}>{item.label}</span>
        </div>
      ))}
    </div>
  );
}

// ─── Date selector row ──────────────────────────────────────
function DateSelector({ dateStr, onPrev, onNext, p, canNext = true }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '12px 16px', background: p.surface,
      borderRadius: '12px', border: `1px solid ${p.border}`,
    }}>
      <div onClick={onPrev} style={{
        width: 36, height: 36, borderRadius: '8px',
        background: p.surfaceAlt, display: 'flex', alignItems: 'center',
        justifyContent: 'center', cursor: 'pointer',
      }}>
        <IconChevronLeft size={18} color={p.text} />
      </div>
      <span style={{
        fontSize: '15px', fontWeight: 600, color: p.text, fontFamily: TYPE.family,
      }}>{dateStr}</span>
      <div onClick={canNext ? onNext : undefined} style={{
        width: 36, height: 36, borderRadius: '8px',
        background: canNext ? p.surfaceAlt : 'transparent',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        cursor: canNext ? 'pointer' : 'default', opacity: canNext ? 1 : 0.3,
      }}>
        <IconChevronRight size={18} color={p.text} />
      </div>
    </div>
  );
}

Object.assign(window, {
  Card, SectionTitle, StatTile, StatusPill, ProgressBar, AppIcon,
  ListItem, TabSwitcher, EmptyState, Btn, InputField, Switch,
  TopBar, BottomNav, DateSelector,
});
