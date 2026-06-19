// app.jsx — Main Parental Monitor app with navigation
// One instance per palette, each wrapped in AndroidDevice inside DesignCanvas.

function ParentalMonitorApp({ p, screenOverride, onScreenChange }) {
  const [screen, setScreen] = React.useState(screenOverride || 'login');
  const [parentTab, setParentTab] = React.useState(0);

  // Sync with external screen state (for canvas focus)
  React.useEffect(() => {
    if (screenOverride && screenOverride !== screen) {
      setScreen(screenOverride);
    }
  }, [screenOverride]);

  const navigate = (target) => {
    setScreen(target);
    if (onScreenChange) onScreenChange(target);
  };

  const parentTabs = [
    { label: 'Dashboard', icon: <IconHome /> },
    { label: 'Lokasi', icon: <IconMapPin /> },
    { label: 'Pesan', icon: <IconMessage /> },
    { label: 'Panggilan', icon: <IconPhone /> },
    { label: 'Lainnya', icon: <IconAppGrid /> },
  ];

  const renderScreen = () => {
    switch (screen) {
      case 'login':
        return <LoginScreen p={p} onLogin={() => navigate('dashboard')} />;
      case 'setup':
        return <SetupScreen p={p} onGenerateCode={() => navigate('pairingwait')} />;
      case 'pairingwait':
        return <PairingWaitScreen p={p} />;
      case 'status':
        return <StatusScreen p={p} />;
      case 'pairdevice':
        return <PairDeviceScreen p={p} onBack={() => navigate('dashboard')} onPaired={() => navigate('dashboard')} />;
      case 'dashboard':
        return <DashboardScreen p={p} onNavigate={navigate} />;
      case 'location':
        return <LocationScreen p={p} onBack={() => navigate('dashboard')} />;
      case 'geofence':
        return <HomeGeofenceScreen p={p} onBack={() => navigate('settings')} />;
      case 'messages':
        return <MessagesScreen p={p} onBack={() => navigate('dashboard')} />;
      case 'calls':
        return <CallsScreen p={p} onBack={() => navigate('dashboard')} />;
      case 'appusage':
        return <AppUsageScreen p={p} onBack={() => navigate('dashboard')} />;
      case 'battery':
        return <BatteryScreen p={p} onBack={() => navigate('dashboard')} />;
      case 'settings':
        return <SettingsScreen p={p} onBack={() => navigate('dashboard')} />;
      default:
        return <DashboardScreen p={p} onNavigate={navigate} />;
    }
  };

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: p.bg, minHeight: 0,
    }}>
      {renderScreen()}
    </div>
  );
}

Object.assign(window, { ParentalMonitorApp });
