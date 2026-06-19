// screens-child.jsx — Setup, PairingWait, Status screens (HP Anak)
// Matches SetupActivity, PairingWaitActivity, StatusActivity from Kotlin source.

// ─── Step indicator ─────────────────────────────────────────
function StepDot({ step, current, p }) {
  const active = step <= current;
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: '6px',
    }}>
      <div style={{
        width: 28, height: 28, borderRadius: '50%',
        background: active ? p.primary : p.surfaceAlt,
        color: active ? p.onPrimary : p.textSubtle,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: '13px', fontWeight: 600, fontFamily: TYPE.family,
      }}>{step}</div>
      {step < 3 && (
        <div style={{
          width: 32, height: 2, borderRadius: 1,
          background: step < current ? p.primary : p.border,
        }} />
      )}
    </div>
  );
}

// ─── Setup Screen ───────────────────────────────────────────
function SetupScreen({ p, onGenerateCode }) {
  const [step, setStep] = React.useState(1);
  const [perms, setPerms] = React.useState({
    location: false, sms: false, calls: false, phone: false, notif: false,
  });
  const [special, setSpecial] = React.useState({
    accessibility: false, notifListener: false, usageStats: false,
  });
  const [batteryOpt, setBatteryOpt] = React.useState(false);

  const togglePerm = (key) => setPerms(prev => ({ ...prev, [key]: !prev[key] }));
  const toggleSpecial = (key) => setSpecial(prev => ({ ...prev, [key]: !prev[key] }));

  const allPerms = Object.values(perms).every(Boolean);
  const allSpecial = Object.values(special).every(Boolean);

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      {/* Header */}
      <div style={{
        padding: '16px 20px', background: p.primary, color: p.onPrimary,
      }}>
        <div style={{ fontSize: '18px', fontWeight: 600 }}>Setup Monitoring</div>
        <div style={{ fontSize: '12px', opacity: 0.85, marginTop: '4px' }}>
          Konfigurasi HP anak untuk pemantauan
        </div>
      </div>

      {/* Step indicator */}
      <div style={{
        display: 'flex', justifyContent: 'center', gap: '12px',
        padding: '20px 0 8px',
      }}>
        <StepDot step={1} current={step} p={p} />
        <StepDot step={2} current={step} p={p} />
        <StepDot step={3} current={step} p={p} />
      </div>

      {/* Content */}
      <div style={{ flex: 1, padding: '16px 20px', overflow: 'auto' }}>
        {step === 1 && (
          <div>
            <div style={{ fontSize: '16px', fontWeight: 600, color: p.text, marginBottom: '12px' }}>
              📍 Permission Dasar
            </div>
            {[
              { key: 'location', label: 'Lokasi', desc: 'Akses lokasi GPS' },
              { key: 'sms', label: 'SMS', desc: 'Baca pesan SMS' },
              { key: 'calls', label: 'Panggilan', desc: 'Baca log panggilan' },
              { key: 'phone', label: 'Phone State', desc: 'Status telepon' },
              { key: 'notif', label: 'Notifikasi', desc: 'Kirim notifikasi' },
            ].map(perm => (
              <Card key={perm.key} p={p} style={{
                marginBottom: '10px', padding: '14px',
                display: 'flex', alignItems: 'center', gap: '12px',
              }}>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: '14px', fontWeight: 500, color: p.text }}>{perm.label}</div>
                  <div style={{ fontSize: '12px', color: p.textMuted }}>{perm.desc}</div>
                </div>
                <div onClick={() => togglePerm(perm.key)} style={{
                  padding: '8px 14px', borderRadius: '8px', fontSize: '13px', fontWeight: 600,
                  background: perms[perm.key] ? p.success + '20' : p.primary + '15',
                  color: perms[perm.key] ? p.success : p.primary,
                  cursor: 'pointer',
                }}>
                  {perms[perm.key] ? '✓ Aktif' : 'Grant'}
                </div>
              </Card>
            ))}
          </div>
        )}

        {step === 2 && (
          <div>
            <div style={{ fontSize: '16px', fontWeight: 600, color: p.text, marginBottom: '12px' }}>
              🔧 Permission Khusus
            </div>
            {[
              { key: 'accessibility', label: 'Accessibility Service', desc: 'Baca chat WhatsApp' },
              { key: 'notifListener', label: 'Notification Listener', desc: 'Tangkap notifikasi WhatsApp' },
              { key: 'usageStats', label: 'Usage Stats Access', desc: 'Pantau penggunaan aplikasi' },
            ].map(perm => (
              <Card key={perm.key} p={p} style={{
                marginBottom: '10px', padding: '14px',
                display: 'flex', alignItems: 'center', gap: '12px',
              }}>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: '14px', fontWeight: 500, color: p.text }}>{perm.label}</div>
                  <div style={{ fontSize: '12px', color: p.textMuted }}>{perm.desc}</div>
                </div>
                <div onClick={() => toggleSpecial(perm.key)} style={{
                  padding: '8px 14px', borderRadius: '8px', fontSize: '13px', fontWeight: 600,
                  background: special[perm.key] ? p.success + '20' : p.warning + '20',
                  color: special[perm.key] ? p.success : p.warning,
                  cursor: 'pointer',
                }}>
                  {special[perm.key] ? '✓ Aktif' : 'Buka Settings'}
                </div>
              </Card>
            ))}
          </div>
        )}

        {step === 3 && (
          <div>
            <div style={{ fontSize: '16px', fontWeight: 600, color: p.text, marginBottom: '12px' }}>
              🔋 Optimasi Baterai
            </div>
            <Card p={p} style={{ padding: '16px' }}>
              <div style={{ fontSize: '14px', fontWeight: 500, color: p.text, marginBottom: '8px' }}>
                Nonaktifkan Battery Optimization
              </div>
              <div style={{ fontSize: '12px', color: p.textMuted, marginBottom: '14px' }}>
                Agar monitoring berjalan terus di background
              </div>
              <div onClick={() => setBatteryOpt(!batteryOpt)} style={{
                padding: '10px 16px', borderRadius: '8px', fontSize: '13px', fontWeight: 600,
                textAlign: 'center',
                background: batteryOpt ? p.success + '20' : p.primary + '15',
                color: batteryOpt ? p.success : p.primary,
                cursor: 'pointer',
              }}>
                {batteryOpt ? '✓ Sudah dioptimalkan' : 'Buka Battery Settings'}
              </div>
            </Card>

            {/* Note card */}
            <Card p={p} style={{
              marginTop: '16px', padding: '14px',
              background: p.surfaceAlt, border: `1px solid ${p.border}`,
            }}>
              <div style={{ fontSize: '12px', color: p.textMuted, lineHeight: '1.6' }}>
                <strong style={{ color: p.text }}>Catatan:</strong><br />
                • Aplikasi berjalan di background & tersembunyi dari launcher<br />
                • Auto-start setelah restart HP<br />
                • Akses via dialer: <code style={{ background: p.bg, padding: '2px 6px', borderRadius: '4px' }}>*#*#1234#*#*</code>
              </div>
            </Card>
          </div>
        )}
      </div>

      {/* Bottom action */}
      <div style={{ padding: '16px 20px' }}>
        {step < 3 ? (
          <Btn
            p={p}
            fullWidth
            onClick={() => setStep(step + 1)}
            disabled={(step === 1 && !allPerms) || (step === 2 && !allSpecial)}
          >
            Lanjutkan
          </Btn>
        ) : (
          <Btn p={p} fullWidth onClick={onGenerateCode} disabled={!batteryOpt}>
            Mulai Monitoring
          </Btn>
        )}
      </div>
    </div>
  );
}


// ─── Pairing Wait Screen ────────────────────────────────────
function PairingWaitScreen({ p }) {
  const [seconds, setSeconds] = React.useState(600);
  const code = '482917';

  React.useEffect(() => {
    const timer = setInterval(() => setSeconds(s => Math.max(0, s - 1)), 1000);
    return () => clearInterval(timer);
  }, []);

  const mm = String(Math.floor(seconds / 60)).padStart(2, '0');
  const ss = String(seconds % 60).padStart(2, '0');
  const paired = seconds < 580; // simulate pairing after 20s

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center',
      background: p.bg, fontFamily: TYPE.family, padding: '32px',
    }}>
      {/* Icon */}
      <div style={{
        width: 80, height: 80, borderRadius: '50%',
        background: paired ? p.success + '20' : p.primary + '15',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        marginBottom: '24px',
      }}>
        {paired
          ? <IconCheckCircle size={40} color={p.success} />
          : <IconClock size={40} color={p.primary} />
        }
      </div>

      <div style={{ fontSize: '18px', fontWeight: 600, color: p.text, marginBottom: '8px' }}>
        {paired ? 'Berhasil Terhubung!' : 'Menunggu Pairing...'}
      </div>
      <div style={{ fontSize: '13px', color: p.textMuted, marginBottom: '28px', textAlign: 'center' }}>
        {paired ? 'HP orang tua telah terhubung' : 'Masukkan kode ini di HP orang tua'}
      </div>

      {/* Code display */}
      {!paired && (
        <Card p={p} elevation style={{
          padding: '24px 32px', textAlign: 'center', marginBottom: '20px',
        }}>
          <div style={{
            fontSize: '36px', fontWeight: 700, color: p.primary,
            fontFamily: 'monospace', letterSpacing: '8px',
          }}>
            {code}
          </div>
        </Card>
      )}

      {/* Countdown */}
      {!paired && (
        <div style={{
          fontSize: '20px', fontWeight: 600, color: seconds < 120 ? p.danger : p.text,
          fontFamily: 'monospace',
        }}>
          {mm}:{ss}
        </div>
      )}
    </div>
  );
}


// ─── Status Screen ──────────────────────────────────────────
function StatusScreen({ p }) {
  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center',
      background: p.bg, fontFamily: TYPE.family, padding: '32px',
    }}>
      <div style={{
        width: 80, height: 80, borderRadius: '50%',
        background: p.success + '20',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        marginBottom: '20px',
      }}>
        <IconShield size={40} color={p.success} />
      </div>

      <div style={{ fontSize: '22px', fontWeight: 700, color: p.success, marginBottom: '8px' }}>
        Monitoring Aktif
      </div>
      <div style={{ fontSize: '14px', color: p.textMuted, textAlign: 'center' }}>
        HP ini sedang dipantau oleh orang tua
      </div>
    </div>
  );
}

Object.assign(window, { SetupScreen, PairingWaitScreen, StatusScreen });
