// palettes.jsx — 3 calm color palette variations for Parental Monitor redesign
// Each palette defines a complete Material-style token set so screens can be themed
// by switching which palette object is active. Tokens follow calm aesthetic:
// muted, low-saturation, soft contrast, warm/cool balance.

// ─────────────────────────────────────────────────────────────
// PALETTE 1 — Sage Green & Cream (earthy, natural, grounded)
// ─────────────────────────────────────────────────────────────
const PALETTE_SAGE = {
  name: 'Sage & Cream',
  tagline: 'Earthy · Natural · Grounded',
  bg: '#F5F2EC',          // warm cream background
  surface: '#FFFFFF',     // cards
  surfaceAlt: '#EAE6DC',  // subtle elevated surface
  border: '#D9D3C4',      // soft border
  primary: '#7C9885',     // sage green — calming, trust
  primarySoft: '#A8BFA8',
  primaryDark: '#5A7560',
  onPrimary: '#FFFFFF',
  secondary: '#C2A878',   // warm sand accent
  accent: '#9C8AA5',      // dusty mauve accent for variety
  text: '#2F3A33',        // deep forest text
  textMuted: '#6B7568',   // warm gray text
  textSubtle: '#9A9F8E',
  success: '#8FAD8B',     // muted green (was 4CAF50)
  warning: '#D4A574',     // muted amber (was FF9800)
  danger: '#C17B6E',      // muted terracotta (was F44336)
  info: '#8AA1B5',        // dusty blue (was 2196F3)
  shadow: 'rgba(85, 100, 80, 0.08)',
};

// ─────────────────────────────────────────────────────────────
// PALETTE 2 — Dusty Blue & Soft White (cool, airy, serene)
// ─────────────────────────────────────────────────────────────
const PALETTE_BLUE = {
  name: 'Dusty Blue',
  tagline: 'Cool · Airy · Serene',
  bg: '#F2F4F7',          // soft white-blue background
  surface: '#FFFFFF',
  surfaceAlt: '#E5EAF0',
  border: '#D2D9E2',
  primary: '#7B95B3',     // dusty blue
  primarySoft: '#B0C2D6',
  primaryDark: '#5B7593',
  onPrimary: '#FFFFFF',
  secondary: '#A8B8C9',
  accent: '#B8A8C8',      // mauve accent
  text: '#2A3548',        // deep slate text
  textMuted: '#5F6B82',
  textSubtle: '#9AA5B5',
  success: '#92B098',     // sage-leaning green
  warning: '#D4B58E',     // warm sand
  danger: '#C99B95',      // dusty rose
  info: '#8AA1B5',
  shadow: 'rgba(60, 80, 110, 0.08)',
};

// ─────────────────────────────────────────────────────────────
// PALETTE 3 — Mauve & Sand (warm lavender, soft, personal)
// ─────────────────────────────────────────────────────────────
const PALETTE_MAUVE = {
  name: 'Mauve & Sand',
  tagline: 'Warm · Soft · Personal',
  bg: '#F5F0EA',          // warm sand background
  surface: '#FFFFFF',
  surfaceAlt: '#EBE3DA',
  border: '#DCD0C2',
  primary: '#A893B8',     // soft mauve
  primarySoft: '#C9BBD4',
  primaryDark: '#7E6991',
  onPrimary: '#FFFFFF',
  secondary: '#C9B89C',   // sand accent
  accent: '#93B8A8',      // sage accent
  text: '#3D2F47',        // deep aubergine text
  textMuted: '#6E5F77',
  textSubtle: '#A39AAA',
  success: '#9DB89C',
  warning: '#D4B58E',
  danger: '#C99B95',
  info: '#8AA1B5',
  shadow: 'rgba(110, 90, 120, 0.08)',
};

// All three palettes available for the canvas
const PALETTES = {
  sage: PALETTE_SAGE,
  blue: PALETTE_BLUE,
  mauve: PALETTE_MAUVE,
};

// Typography (shared across all palettes)
const TYPE = {
  family: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", system-ui, sans-serif',
  sizeXs: '11px',
  sizeSm: '13px',
  sizeMd: '14px',
  sizeBase: '15px',
  sizeLg: '17px',
  sizeXl: '20px',
  size2xl: '24px',
  size3xl: '28px',
  size4xl: '34px',
  weightNormal: 400,
  weightMedium: 500,
  weightSemibold: 600,
  weightBold: 700,
};

const SPACE = {
  xs: '4px',
  sm: '8px',
  md: '12px',
  lg: '16px',
  xl: '20px',
  xxl: '24px',
  xxxl: '32px',
};

const RADIUS = {
  sm: '6px',
  md: '10px',
  lg: '14px',
  xl: '20px',
  xxl: '28px',
  pill: '999px',
};

Object.assign(window, { PALETTE_SAGE, PALETTE_BLUE, PALETTE_MAUVE, PALETTES, TYPE, SPACE, RADIUS });