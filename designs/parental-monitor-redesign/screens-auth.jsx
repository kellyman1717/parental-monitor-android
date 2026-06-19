// screens-auth.jsx — Login / Register screen
// Matches LoginActivity from the Kotlin source.

function LoginScreen({ p, onLogin }) {
  const [email, setEmail] = React.useState('');
  const [password, setPassword] = React.useState('');
  const [showPassword, setShowPassword] = React.useState(false);
  const [isRegister, setIsRegister] = React.useState(false);
  const [loading, setLoading] = React.useState(false);

  const handleLogin = () => {
    if (!email || !password) return;
    setLoading(true);
    setTimeout(() => { setLoading(false); onLogin(); }, 1200);
  };

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      {/* Header area with gradient */}
      <div style={{
        padding: '48px 24px 40px',
        background: `linear-gradient(135deg, ${p.primary}22, ${p.primary}08)`,
        textAlign: 'center',
      }}>
        {/* Shield logo */}
        <div style={{
          width: 72, height: 72, borderRadius: '20px',
          background: p.primary + '15',
          display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
          marginBottom: '16px',
        }}>
          <IconShield size={36} color={p.primary} />
        </div>
        <div style={{ fontSize: '24px', fontWeight: 700, color: p.text, marginBottom: '6px' }}>
          Parental Monitor
        </div>
        <div style={{ fontSize: '14px', color: p.textMuted }}>
          {isRegister ? 'Buat akun baru' : 'Masuk ke akun Anda'}
        </div>
      </div>

      {/* Form card */}
      <div style={{ padding: '0 20px', marginTop: '-20px' }}>
        <Card p={p} elevation style={{ padding: '20px' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <InputField
              label="Email"
              value={email}
              onChange={setEmail}
              placeholder="email@example.com"
              type="email"
              prefix={<IconMail size={16} color={p.textSubtle} />}
              p={p}
            />
            <div>
              <InputField
                label="Password"
                value={password}
                onChange={setPassword}
                placeholder="Minimal 6 karakter"
                type={showPassword ? 'text' : 'password'}
                prefix={<IconLock size={16} color={p.textSubtle} />}
                suffix={
                  <div onClick={() => setShowPassword(!showPassword)} style={{ cursor: 'pointer' }}>
                    {showPassword
                      ? <IconEyeOff size={16} color={p.textSubtle} />
                      : <IconEye size={16} color={p.textSubtle} />}
                  </div>
                }
                p={p}
              />
            </div>

            <Btn p={p} fullWidth onClick={handleLogin} disabled={loading || !email || !password}>
              {loading ? (
                <span style={{
                  display: 'inline-block', width: 18, height: 18,
                  border: `2px solid ${p.onPrimary}40`, borderTopColor: p.onPrimary,
                  borderRadius: '50%', animation: 'spin 0.6s linear infinite',
                }} />
              ) : (
                isRegister ? 'Daftar' : 'Masuk'
              )}
            </Btn>
          </div>
        </Card>
      </div>

      {/* Toggle login/register */}
      <div style={{
        textAlign: 'center', padding: '20px',
        fontSize: '14px', color: p.textMuted,
      }}>
        {isRegister ? 'Sudah punya akun? ' : 'Belum punya akun? '}
        <span
          onClick={() => setIsRegister(!isRegister)}
          style={{ color: p.primary, fontWeight: 600, cursor: 'pointer' }}
        >
          {isRegister ? 'Masuk' : 'Daftar'}
        </span>
      </div>

      {/* Footer */}
      <div style={{
        marginTop: 'auto', textAlign: 'center', padding: '16px',
        fontSize: '12px', color: p.textSubtle,
      }}>
        Pantau & lindungi anak Anda
      </div>
    </div>
  );
}

Object.assign(window, { LoginScreen });
