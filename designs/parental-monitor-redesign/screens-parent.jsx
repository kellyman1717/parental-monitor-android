// screens-parent.jsx — All 9 Parent screens
// Dashboard, PairDevice, Location, HomeGeofence, Messages, Calls, AppUsage, Battery, Settings

// ─── Mock data ──────────────────────────────────────────────
const MOCK_LOCATIONS = [
  { lat: -6.2088, lng: 106.8456, accuracy: 12, time: '14:32' },
  { lat: -6.2091, lng: 106.8450, accuracy: 8, time: '14:17' },
  { lat: -6.2100, lng: 106.8460, accuracy: 15, time: '13:52' },
  { lat: -6.2095, lng: 106.8445, accuracy: 10, time: '13:22' },
  { lat: -6.2082, lng: 106.8470, accuracy: 20, time: '12:45' },
];

const MOCK_SMS = [
  { sender: 'Bank BCA', body: 'Rp 150.000 telah ditransfer ke rekening...', time: '13:20', isIncoming: true },
  { sender: 'Andi', body: 'Aku sudah sampai sekolah', time: '12:05', isIncoming: true },
  { sender: 'Mama', body: 'Jangan lupa makan siang ya', time: '11:30', isIncoming: false },
  { sender: '0812xxxx', body: 'Paket Anda sedang dikirim', time: '10:15', isIncoming: true },
];

const MOCK_WHATSAPP = [
  { sender: 'Grup Kelas 9A', body: 'Tugas dikumpulkan besok ya', time: '14:00', isGroup: true },
  { sender: 'Rina', body: 'Haha iya betul', time: '13:45', isGroup: false },
  { sender: 'Papa', body: 'Pulang jam berapa?', time: '12:30', isGroup: false },
];

const MOCK_CALLS = [
  { name: 'Mama', type: 'incoming', duration: 180, time: '14:10', date: '19/06' },
  { name: 'Papa', type: 'outgoing', duration: 45, time: '13:30', date: '19/06' },
  { name: '0812xxxx', type: 'missed', duration: 0, time: '12:15', date: '19/06' },
  { name: 'Andi', type: 'incoming', duration: 320, time: '11:00', date: '19/06' },
  { name: 'Rina', type: 'outgoing', duration: 120, time: '10:30', date: '19/06' },
];

const MOCK_APPS = [
  { name: 'TikTok', pkg: 'com.zhiliaoapp.musically', duration: 14400000, opens: 12 },
  { name: 'Instagram', pkg: 'com.instagram.android', duration: 9000000, opens: 8 },
  { name: 'WhatsApp', pkg: 'com.whatsapp', duration: 7200000, opens: 25 },
  { name: 'YouTube', pkg: 'com.google.android.youtube', duration: 5400000, opens: 4 },
  { name: 'Chrome', pkg: 'com.android.chrome', duration: 3600000, opens: 6 },
  { name: 'Spotify', pkg: 'com.spotify.music', duration: 2700000, opens: 3 },
];

const MOCK_BATTERY_APPS = [
  { name: 'TikTok', pkg: 'com.zhiliaoapp.musically', drain: 12.5, fgMs: 14400000, opens: 12 },
  { name: 'Instagram', pkg: 'com.instagram.android', drain: 8.3, fgMs: 9000000, opens: 8 },
  { name: 'WhatsApp', pkg: 'com.whatsapp', drain: 6.1, fgMs: 7200000, opens: 25 },
  { name: 'YouTube', pkg: 'com.google.android.youtube', drain: 5.8, fgMs: 5400000, opens: 4 },
  { name: 'Gmail', pkg: 'com.google.android.gm', drain: 2.1, fgMs: 1800000, opens: 5 },
];

// ─── Duration formatter ─────────────────────────────────────
function fmtDuration(ms) {
  const h = Math.floor(ms / 3600000);
  const m = Math.floor((ms % 3600000) / 60000);
  if (h > 0) return `${h}j ${m}m`;
  if (m > 0) return `${m}m`;
  return '<1m';
}

function fmtCallDuration(sec) {
  if (sec < 60) return `${sec}s`;
  if (sec < 3600) return `${Math.floor(sec/60)}m ${sec%60}s`;
  return `${Math.floor(sec/3600)}h ${Math.floor((sec%3600)/60)}m`;
}


// ═════════════════════════════════════════════════════════════
// DASHBOARD
// ═════════════════════════════════════════════════════════════
function DashboardScreen({ p, onNavigate }) {
  const childName = 'Xiaomi Redmi Note 12';
  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      {/* Top bar */}
      <div style={{
        padding: '10px 16px', background: p.primary, color: p.onPrimary,
        display: 'flex', alignItems: 'center', gap: '8px',
      }}>
        <IconShield size={22} color="#fff" />
        <span style={{ flex: 1, fontSize: '17px', fontWeight: 600 }}>Dashboard</span>
        <div onClick={() => onNavigate('settings')} style={{ cursor: 'pointer', padding: 6 }}>
          <IconSettings size={20} color="#fff" />
        </div>
      </div>

      {/* Device info bar */}
      <div style={{
        padding: '10px 16px', background: p.primary + '12',
        display: 'flex', alignItems: 'center', gap: '8px',
        borderBottom: `1px solid ${p.border}`,
      }}>
        <IconSmartphone size={16} color={p.primary} />
        <span style={{ fontSize: '13px', fontWeight: 500, color: p.text }}>{childName}</span>
        <span style={{ marginLeft: 'auto', fontSize: '11px', color: p.textMuted }}>
          <StatusPill label="Terhubung" color={p.success} p={p} />
        </span>
      </div>

      <div style={{ flex: 1, overflow: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {/* Last location card */}
        <Card p={p} onClick={() => onNavigate('location')} style={{ padding: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div style={{
              width: 44, height: 44, borderRadius: '12px',
              background: p.info + '18',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <IconMapPin size={22} color={p.info} />
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: '12px', color: p.textMuted }}>Lokasi Terakhir</div>
              <div style={{ fontSize: '14px', fontWeight: 500, color: p.text }}>
                -6.2088, 106.8456
              </div>
              <div style={{ fontSize: '11px', color: p.textSubtle }}>14:32 • akurasi 12m</div>
            </div>
            <IconChevronRight size={18} color={p.textSubtle} />
          </div>
        </Card>

        {/* 3 stat cards */}
        <div style={{ display: 'flex', gap: '10px' }}>
          <StatTile value="128" label="SMS" icon={<IconMessage />} p={p} accentColor={p.info} />
          <StatTile value="47" label="Panggilan" icon={<IconPhone />} p={p} accentColor={p.success} />
          <StatTile value="203" label="WhatsApp" icon={<IconMessage />} p={p} accentColor={p.accent} />
        </div>

        {/* Recent messages */}
        <SectionTitle p={p} action="Lihat Semua" onAction={() => onNavigate('messages')}>
          Pesan Terbaru
        </SectionTitle>
        <Card p={p} style={{ padding: '4px 12px' }}>
          {MOCK_SMS.slice(0, 3).map((sms, i) => (
            <ListItem
              key={i}
              leading={<AppIcon name={sms.sender} p={p} size={34} color={sms.isIncoming ? p.info : p.success} />}
              title={sms.sender}
              subtitle={sms.body.substring(0, 36) + '...'}
              trailing={<span style={{ fontSize: '11px', color: p.textSubtle }}>{sms.time}</span>}
              p={p}
              dense
            />
          ))}
        </Card>

        {/* Recent calls */}
        <SectionTitle p={p} action="Lihat Semua" onAction={() => onNavigate('calls')}>
          Panggilan Terbaru
        </SectionTitle>
        <Card p={p} style={{ padding: '4px 12px' }}>
          {MOCK_CALLS.slice(0, 3).map((call, i) => (
            <ListItem
              key={i}
              leading={<AppIcon name={call.name} p={p} size={34} color={
                call.type === 'missed' ? p.danger : call.type === 'incoming' ? p.success : p.info
              } />}
              title={call.name}
              subtitle={`${call.type === 'missed' ? 'Tak Terjawab' : call.type === 'incoming' ? 'Masuk' : 'Keluar'} • ${fmtCallDuration(call.duration)}`}
              trailing={<span style={{ fontSize: '11px', color: p.textSubtle }}>{call.time}</span>}
              p={p}
              dense
            />
          ))}
        </Card>

        {/* Menu grid */}
        <div style={{
          display: 'grid', gridTemplateColumns: '1fr 1fr 1fr',
          gap: '10px',
        }}>
          {[
            { icon: <IconMapPin />, label: 'Lokasi', target: 'location', color: p.info },
            { icon: <IconAppGrid />, label: 'Aplikasi', target: 'appusage', color: p.warning },
            { icon: <IconMessage />, label: 'Pesan', target: 'messages', color: p.accent },
            { icon: <IconBattery />, label: 'Baterai', target: 'battery', color: p.success },
            { icon: <IconSettings />, label: 'Pengaturan', target: 'settings', color: p.textMuted },
          ].map((item, i) => (
            <Card key={i} p={p} onClick={() => onNavigate(item.target)} style={{
              padding: '16px 8px', textAlign: 'center', cursor: 'pointer',
            }}>
              {React.cloneElement(item.icon, { size: 22, color: item.color })}
              <div style={{ fontSize: '12px', fontWeight: 500, color: p.text, marginTop: '6px' }}>
                {item.label}
              </div>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}


// ═════════════════════════════════════════════════════════════
// PAIR DEVICE
// ═════════════════════════════════════════════════════════════
function PairDeviceScreen({ p, onBack, onPaired }) {
  const [code, setCode] = React.useState('');
  const [loading, setLoading] = React.useState(false);

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      <TopBar title="Pair HP Anak" p={p} onBack={onBack} />

      <div style={{ flex: 1, padding: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {/* Illustration */}
        <div style={{ textAlign: 'center', padding: '24px 0 8px' }}>
          <div style={{
            width: 64, height: 64, borderRadius: '16px',
            background: p.primary + '15',
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            marginBottom: '12px',
          }}>
            <IconLink size={32} color={p.primary} />
          </div>
          <div style={{ fontSize: '15px', color: p.textMuted }}>
            Masukkan 6 digit kode dari HP anak
          </div>
        </div>

        {/* Code input */}
        <Card p={p} elevation style={{ padding: '20px' }}>
          <InputField
            label="Kode Pairing"
            value={code}
            onChange={v => setCode(v.replace(/\D/g, '').substring(0, 6))}
            placeholder="000000"
            p={p}
          />
          <div style={{ marginTop: '16px' }}>
            <Btn
              p={p}
              fullWidth
              disabled={code.length !== 6 || loading}
              onClick={() => {
                setLoading(true);
                setTimeout(() => { setLoading(false); onPaired(); }, 1500);
              }}
            >
              {loading ? 'Menghubungkan...' : 'Hubungkan'}
            </Btn>
          </div>
        </Card>

        {/* Info card */}
        <Card p={p} style={{ padding: '14px', background: p.surfaceAlt }}>
          <div style={{ fontSize: '12px', color: p.textMuted, lineHeight: '1.7' }}>
            <strong style={{ color: p.text }}>Cara pairing:</strong><br />
            1. Install aplikasi di HP anak<br />
            2. Buka via dialer <code style={{ background: p.bg, padding: '2px 5px', borderRadius: '4px' }}>*#*#1234#*#*</code><br />
            3. Ikuti setup hingga mendapat kode 6 digit<br />
            4. Masukkan kode di sini
          </div>
        </Card>
      </div>
    </div>
  );
}


// ═════════════════════════════════════════════════════════════
// LOCATION
// ═════════════════════════════════════════════════════════════
function LocationScreen({ p, onBack }) {
  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      <TopBar title="📍 Riwayat Lokasi" p={p} onBack={onBack} />

      <div style={{ flex: 1, overflow: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {/* Last location card */}
        <Card p={p} elevation style={{ padding: '16px' }}>
          <div style={{ fontSize: '12px', color: p.textMuted, marginBottom: '6px' }}>Lokasi Terakhir</div>
          <div style={{ fontSize: '18px', fontWeight: 600, color: p.text, fontFamily: 'monospace' }}>
            {MOCK_LOCATIONS[0].lat.toFixed(6)}, {MOCK_LOCATIONS[0].lng.toFixed(6)}
          </div>
          <div style={{ display: 'flex', gap: '16px', marginTop: '8px' }}>
            <span style={{ fontSize: '12px', color: p.textMuted }}>
              Akurasi: <strong style={{ color: p.text }}>{MOCK_LOCATIONS[0].accuracy}m</strong>
            </span>
            <span style={{ fontSize: '12px', color: p.textMuted }}>
              Jam: <strong style={{ color: p.text }}>{MOCK_LOCATIONS[0].time}</strong>
            </span>
          </div>
        </Card>

        {/* History */}
        <SectionTitle p={p}>Riwayat</SectionTitle>
        {MOCK_LOCATIONS.map((loc, i) => (
          <Card key={i} p={p} style={{ padding: '12px 14px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{
                width: 10, height: 10, borderRadius: '50%',
                background: i === 0 ? p.success : p.border,
                flexShrink: 0,
              }} />
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: '13px', fontWeight: 500, color: p.text, fontFamily: 'monospace' }}>
                  {loc.lat.toFixed(4)}, {loc.lng.toFixed(4)}
                </div>
                <div style={{ fontSize: '11px', color: p.textSubtle }}>
                  akurasi {loc.accuracy}m
                </div>
              </div>
              <span style={{ fontSize: '12px', color: p.textMuted, fontFamily: 'monospace' }}>
                {loc.time}
              </span>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}


// ═════════════════════════════════════════════════════════════
// HOME GEOFENCE
// ═════════════════════════════════════════════════════════════
function HomeGeofenceScreen({ p, onBack }) {
  const [radius, setRadius] = React.useState(200);
  const [hasHome, setHasHome] = React.useState(true);
  const status = 'INSIDE';

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      <TopBar title="🏠 Geofence Rumah" p={p} onBack={onBack} />

      <div style={{ flex: 1, overflow: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {/* Status */}
        <Card p={p} elevation style={{
          padding: '16px', textAlign: 'center',
          borderLeft: `4px solid ${status === 'INSIDE' ? p.success : p.danger}`,
        }}>
          <div style={{ fontSize: '13px', color: p.textMuted, marginBottom: '4px' }}>Status Saat Ini</div>
          <StatusPill
            label={status === 'INSIDE' ? '✓ Di Dalam Rumah' : '✗ Di Luar Rumah'}
            color={status === 'INSIDE' ? p.success : p.danger}
            p={p}
          />
        </Card>

        {/* Info */}
        <Card p={p} style={{ padding: '14px', background: p.surfaceAlt }}>
          <div style={{ fontSize: '12px', color: p.textMuted, lineHeight: '1.6' }}>
            Alarm beep akan berbunyi jika HP keluar dari radius rumah yang ditentukan.
          </div>
        </Card>

        {/* Use current location */}
        <Btn p={p} fullWidth variant="secondary">
          <IconMapPin size={16} color={p.primary} />
          Gunakan Lokasi Saat Ini
        </Btn>

        {/* Manual input */}
        <Card p={p} style={{ padding: '16px' }}>
          <div style={{ fontSize: '14px', fontWeight: 600, color: p.text, marginBottom: '12px' }}>
            Lokasi Rumah
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <InputField label="Latitude" value="-6.2088" onChange={() => {}} p={p} />
            <InputField label="Longitude" value="106.8456" onChange={() => {}} p={p} />
          </div>
        </Card>

        {/* Radius slider */}
        <Card p={p} style={{ padding: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
            <span style={{ fontSize: '14px', fontWeight: 500, color: p.text }}>Radius</span>
            <span style={{ fontSize: '14px', fontWeight: 600, color: p.primary }}>{radius}m</span>
          </div>
          <input
            type="range" min="50" max="1000" step="10"
            value={radius}
            onChange={e => setRadius(Number(e.target.value))}
            style={{ width: '100%', accentColor: p.primary }}
          />
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: p.textSubtle, marginTop: '4px' }}>
            <span>50m</span><span>1000m</span>
          </div>
        </Card>

        <Btn p={p} fullWidth>
          Simpan Lokasi Rumah
        </Btn>

        {hasHome && (
          <Btn p={p} fullWidth variant="ghost" style={{ color: p.danger }}>
            <IconTrash size={16} color={p.danger} />
            Hapus Lokasi Rumah
          </Btn>
        )}
      </div>
    </div>
  );
}


// ═════════════════════════════════════════════════════════════
// MESSAGES (SMS + WhatsApp)
// ═════════════════════════════════════════════════════════════
function MessagesScreen({ p, onBack }) {
  const [tab, setTab] = React.useState(0);

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      <TopBar title="💬 Pesan" p={p} onBack={onBack} />

      <div style={{ padding: '12px 16px 0' }}>
        <TabSwitcher tabs={['SMS (100)', 'WhatsApp (100)']} active={tab} onChange={setTab} p={p} />
      </div>

      <div style={{ flex: 1, overflow: 'auto', padding: '12px 16px' }}>
        {tab === 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {MOCK_SMS.map((sms, i) => (
              <Card key={i} p={p} style={{ padding: '12px 14px' }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                  <div style={{
                    width: 36, height: 36, borderRadius: '10px',
                    background: sms.isIncoming ? p.info + '18' : p.success + '18',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    flexShrink: 0,
                  }}>
                    <IconMessage size={18} color={sms.isIncoming ? p.info : p.success} />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontSize: '14px', fontWeight: 600, color: p.text }}>{sms.sender}</span>
                      <span style={{ fontSize: '11px', color: p.textSubtle }}>{sms.time}</span>
                    </div>
                    <div style={{
                      fontSize: '13px', color: p.textMuted, marginTop: '2px',
                      whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
                    }}>{sms.body}</div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {MOCK_WHATSAPP.map((wa, i) => (
              <Card key={i} p={p} style={{ padding: '12px 14px' }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                  <div style={{
                    width: 36, height: 36, borderRadius: '10px',
                    background: wa.isGroup ? p.accent + '18' : p.success + '18',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    flexShrink: 0,
                  }}>
                    <IconUser size={18} color={wa.isGroup ? p.accent : p.success} />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontSize: '14px', fontWeight: 600, color: p.text }}>
                        {wa.sender}
                        {wa.isGroup && <span style={{ fontSize: '11px', color: p.accent, marginLeft: '4px' }}>👥</span>}
                      </span>
                      <span style={{ fontSize: '11px', color: p.textSubtle }}>{wa.time}</span>
                    </div>
                    <div style={{
                      fontSize: '13px', color: p.textMuted, marginTop: '2px',
                      whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
                    }}>{wa.body}</div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}


// ═════════════════════════════════════════════════════════════
// CALLS
// ═════════════════════════════════════════════════════════════
function CallsScreen({ p, onBack }) {
  const incoming = MOCK_CALLS.filter(c => c.type === 'incoming').length;
  const outgoing = MOCK_CALLS.filter(c => c.type === 'outgoing').length;
  const missed = MOCK_CALLS.filter(c => c.type === 'missed').length;

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      <TopBar title="📞 Riwayat Panggilan" p={p} onBack={onBack} />

      <div style={{ flex: 1, overflow: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {/* Stats summary */}
        <Card p={p} elevation style={{ padding: '16px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-around' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '22px', fontWeight: 700, color: p.success }}>{incoming}</div>
              <div style={{ fontSize: '11px', color: p.textMuted }}>Masuk</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '22px', fontWeight: 700, color: p.info }}>{outgoing}</div>
              <div style={{ fontSize: '11px', color: p.textMuted }}>Keluar</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '22px', fontWeight: 700, color: p.danger }}>{missed}</div>
              <div style={{ fontSize: '11px', color: p.textMuted }}>Tak Jawab</div>
            </div>
          </div>
        </Card>

        {/* Call list */}
        {MOCK_CALLS.map((call, i) => {
          const typeColor = call.type === 'missed' ? p.danger : call.type === 'incoming' ? p.success : p.info;
          return (
            <Card key={i} p={p} style={{ padding: '12px 14px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{
                  width: 40, height: 40, borderRadius: '50%',
                  background: typeColor + '18',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  flexShrink: 0,
                }}>
                  <IconPhone size={20} color={typeColor} />
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: '14px', fontWeight: 600, color: p.text }}>{call.name}</div>
                  <div style={{ display: 'flex', gap: '6px', alignItems: 'center', marginTop: '2px' }}>
                    <span style={{ fontSize: '12px', fontWeight: 500, color: typeColor }}>
                      {call.type === 'missed' ? 'Tak Terjawab' : call.type === 'incoming' ? 'Masuk' : 'Keluar'}
                    </span>
                    <span style={{ fontSize: '12px', color: p.textSubtle }}>•</span>
                    <span style={{ fontSize: '12px', color: p.textSubtle }}>{fmtCallDuration(call.duration)}</span>
                  </div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '12px', color: p.textSubtle }}>{call.date}</div>
                  <div style={{ fontSize: '12px', color: p.textMuted }}>{call.time}</div>
                </div>
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
}


// ═════════════════════════════════════════════════════════════
// APP USAGE
// ═════════════════════════════════════════════════════════════
function AppUsageScreen({ p, onBack }) {
  const totalMs = MOCK_APPS.reduce((s, a) => s + a.duration, 0);
  const maxMs = Math.max(...MOCK_APPS.map(a => a.duration));

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      <TopBar title="📱 Penggunaan Aplikasi" p={p} onBack={onBack} />

      <div style={{ flex: 1, overflow: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {/* Date selector */}
        <DateSelector dateStr="19 Juni 2026" onPrev={() => {}} onNext={() => {}} p={p} canNext={false} />

        {/* Total summary */}
        <Card p={p} elevation style={{
          padding: '20px', textAlign: 'center',
          background: `linear-gradient(135deg, ${p.primary}18, ${p.primary}08)`,
        }}>
          <div style={{ fontSize: '12px', color: p.textMuted }}>Total Penggunaan Hari Ini</div>
          <div style={{ fontSize: '28px', fontWeight: 700, color: p.primary, margin: '6px 0' }}>
            {fmtDuration(totalMs)}
          </div>
          <div style={{ fontSize: '13px', color: p.textMuted }}>{MOCK_APPS.length} aplikasi digunakan</div>
        </Card>

        {/* App list */}
        {MOCK_APPS.map((app, i) => {
          const progress = app.duration / maxMs;
          const barColor = progress > 0.8 ? p.danger : progress > 0.5 ? p.warning : p.success;
          return (
            <Card key={i} p={p} style={{ padding: '14px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                <AppIcon name={app.name} p={p} color={p.warning} />
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: '14px', fontWeight: 600, color: p.text }}>{app.name}</div>
                  <div style={{ fontSize: '11px', color: p.textSubtle }}>{app.pkg}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '15px', fontWeight: 700, color: p.text }}>{fmtDuration(app.duration)}</div>
                  <div style={{ fontSize: '11px', color: p.textSubtle }}>{app.opens}x dibuka</div>
                </div>
              </div>
              <ProgressBar value={app.duration} max={maxMs} color={barColor} p={p} />
            </Card>
          );
        })}
      </div>
    </div>
  );
}


// ═════════════════════════════════════════════════════════════
// BATTERY USAGE
// ═════════════════════════════════════════════════════════════
function BatteryScreen({ p, onBack }) {
  const [tab, setTab] = React.useState(0);
  const level = 68;
  const isCharging = false;
  const batteryColor = level > 60 ? p.success : level > 20 ? p.warning : p.danger;

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      <TopBar title="🔋 Penggunaan Baterai" p={p} onBack={onBack} />

      <div style={{ flex: 1, overflow: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {/* Battery status card */}
        <Card p={p} elevation style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <div>
              <div style={{ fontSize: '17px', fontWeight: 600, color: p.text }}>Status Baterai</div>
              <div style={{ fontSize: '13px', color: p.textMuted }}>
                {isCharging ? 'Mengisi (USB)' : 'Tidak mengisi'}
              </div>
            </div>
            <div style={{
              width: 64, height: 64, borderRadius: '50%',
              background: batteryColor + '18',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <span style={{ fontSize: '20px', fontWeight: 700, color: batteryColor }}>{level}%</span>
            </div>
          </div>

          {/* Details row */}
          <div style={{ display: 'flex', justifyContent: 'space-around', marginBottom: '14px' }}>
            <div style={{ textAlign: 'center' }}>
              <IconThermometer size={20} color={p.info} />
              <div style={{ fontSize: '13px', fontWeight: 500, color: p.text, marginTop: '2px' }}>32.5°C</div>
              <div style={{ fontSize: '10px', color: p.textSubtle }}>Suhu</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <IconZap size={20} color={p.warning} />
              <div style={{ fontSize: '13px', fontWeight: 500, color: p.text, marginTop: '2px' }}>4200 mV</div>
              <div style={{ fontSize: '10px', color: p.textSubtle }}>Voltase</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <IconHeart size={20} color={p.success} />
              <div style={{ fontSize: '13px', fontWeight: 500, color: p.text, marginTop: '2px' }}>Baik</div>
              <div style={{ fontSize: '10px', color: p.textSubtle }}>Kesehatan</div>
            </div>
          </div>

          <ProgressBar value={level} max={100} color={batteryColor} p={p} height={10} />
        </Card>

        {/* Tabs */}
        <TabSwitcher tabs={['📊 Peringkat', '📱 Semua App']} active={tab} onChange={setTab} p={p} />

        {/* App list */}
        <div style={{ fontSize: '14px', fontWeight: 600, color: p.text }}>
          {tab === 0 ? 'Aplikasi Paling Boros' : `Semua Aplikasi (${MOCK_BATTERY_APPS.length})`}
        </div>

        {MOCK_BATTERY_APPS.map((app, i) => {
          const rank = i + 1;
          const rankColor = rank === 1 ? '#D4A017' : rank === 2 ? '#A8A8A8' : rank === 3 ? '#B87333' : p.textSubtle;
          const drainColor = app.drain > 10 ? p.danger : app.drain > 5 ? p.warning : p.success;

          return (
            <Card key={i} p={p} style={{ padding: '14px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
                {/* Rank badge */}
                <div style={{
                  width: 28, height: 28, borderRadius: '50%',
                  background: rank <= 3 ? rankColor : p.surfaceAlt,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '12px', fontWeight: 700,
                  color: rank <= 3 ? '#fff' : p.textSubtle,
                }}>{rank}</div>
                <AppIcon name={app.name} p={p} color={drainColor} size={36} />
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: '14px', fontWeight: 600, color: p.text }}>{app.name}</div>
                  <div style={{ fontSize: '11px', color: p.textSubtle }}>{app.pkg}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: '14px', fontWeight: 700, color: p.text }}>{fmtDuration(app.fgMs)}</div>
                  <div style={{ fontSize: '12px', fontWeight: 600, color: drainColor }}>
                    ~{app.drain.toFixed(1)}%
                  </div>
                </div>
              </div>
              <ProgressBar value={app.fgMs} max={MOCK_BATTERY_APPS[0].fgMs} color={drainColor} p={p} />
              <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '4px' }}>
                <span style={{ fontSize: '10px', color: p.textSubtle }}>{app.opens}x dibuka</span>
                <span style={{ fontSize: '10px', color: p.textSubtle }}>Terakhir: 14:30</span>
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
}


// ═════════════════════════════════════════════════════════════
// SETTINGS
// ═════════════════════════════════════════════════════════════
function SettingsScreen({ p, onBack }) {
  const [monitoringOn, setMonitoringOn] = React.useState(true);
  const [secretCode, setSecretCode] = React.useState('1234');

  const permissions = [
    { name: 'Lokasi', granted: true },
    { name: 'SMS', granted: true },
    { name: 'Panggilan', granted: true },
    { name: 'Usage Stats', granted: true },
    { name: 'Accessibility', granted: false },
    { name: 'Notification Listener', granted: true },
  ];

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, fontFamily: TYPE.family,
    }}>
      <TopBar title="⚙️ Pengaturan" p={p} onBack={onBack} />

      <div style={{ flex: 1, overflow: 'auto', padding: '16px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
        {/* Account */}
        <Card p={p} style={{ padding: '16px' }}>
          <div style={{ fontSize: '14px', fontWeight: 600, color: p.text, marginBottom: '8px' }}>
            👤 Akun
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: '13px', color: p.textMuted }}>Email</span>
            <span style={{ fontSize: '13px', color: p.text }}>parent@email.com</span>
          </div>
        </Card>

        {/* Geofence */}
        <Card p={p} style={{ padding: '16px' }}>
          <div style={{ fontSize: '14px', fontWeight: 600, color: p.text, marginBottom: '4px' }}>
            🏠 Geofence Rumah
          </div>
          <div style={{ fontSize: '12px', color: p.textMuted, marginBottom: '12px' }}>
            Alarm beep jika HP keluar dari radius rumah
          </div>
          <Btn p={p} fullWidth variant="secondary">
            <IconHome size={16} color={p.primary} />
            Setup Lokasi Rumah
          </Btn>
        </Card>

        {/* Monitoring toggle */}
        <Card p={p} style={{ padding: '16px' }}>
          <div style={{ fontSize: '14px', fontWeight: 600, color: p.text, marginBottom: '10px' }}>
            🔒 Monitoring
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <div style={{ fontSize: '13px', color: p.text }}>Status Monitoring</div>
              <div style={{
                fontSize: '13px', fontWeight: 500,
                color: monitoringOn ? p.success : p.danger,
              }}>
                {monitoringOn ? 'Aktif' : 'Nonaktif'}
              </div>
            </div>
            <Switch checked={monitoringOn} onChange={setMonitoringOn} p={p} />
          </div>
        </Card>

        {/* Secret code */}
        <Card p={p} style={{ padding: '16px' }}>
          <div style={{ fontSize: '14px', fontWeight: 600, color: p.text, marginBottom: '4px' }}>
            🔑 Kode Rahasia
          </div>
          <div style={{ fontSize: '12px', color: p.textMuted, marginBottom: '10px' }}>
            Kode untuk mengakses aplikasi tersembunyi di HP anak
          </div>
          <InputField
            value={secretCode}
            onChange={setSecretCode}
            label="Kode"
            prefix="*#*#"
            suffix="#*#*"
            p={p}
          />
          <div style={{
            marginTop: '8px', fontSize: '12px', color: p.primary,
            fontFamily: 'monospace',
          }}>
            Akses di dialer: *#*#{secretCode}#*#*
          </div>
        </Card>

        {/* Permissions */}
        <Card p={p} style={{ padding: '16px' }}>
          <div style={{ fontSize: '14px', fontWeight: 600, color: p.text, marginBottom: '12px' }}>
            📋 Status Permission
          </div>
          {permissions.map((perm, i) => (
            <div key={i} style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              padding: '8px 0', borderBottom: i < permissions.length - 1 ? `1px solid ${p.border}` : 'none',
            }}>
              <span style={{ fontSize: '13px', color: p.text }}>{perm.name}</span>
              {perm.granted
                ? <IconCheckCircle size={18} color={p.success} />
                : <IconX size={18} color={p.danger} />
              }
            </div>
          ))}
        </Card>

        {/* Device info */}
        <Card p={p} style={{ padding: '16px' }}>
          <div style={{ fontSize: '14px', fontWeight: 600, color: p.text, marginBottom: '10px' }}>
            📱 Info Device
          </div>
          {[
            ['Model', 'Xiaomi Redmi Note 12'],
            ['Android', '14'],
            ['Device ID', 'Xiaomi_Redmi_Note_1...'],
          ].map(([label, val], i) => (
            <div key={i} style={{
              display: 'flex', justifyContent: 'space-between', padding: '6px 0',
            }}>
              <span style={{ fontSize: '13px', color: p.textMuted }}>{label}</span>
              <span style={{ fontSize: '13px', color: p.text }}>{val}</span>
            </div>
          ))}
        </Card>

        {/* Logout */}
        <Btn p={p} fullWidth variant="ghost" style={{ color: p.danger }}>
          <IconLogOut size={16} color={p.danger} />
          Logout
        </Btn>

        <div style={{ height: 8 }} />
      </div>
    </div>
  );
}


Object.assign(window, {
  DashboardScreen, PairDeviceScreen, LocationScreen, HomeGeofenceScreen,
  MessagesScreen, CallsScreen, AppUsageScreen, BatteryScreen, SettingsScreen,
});
