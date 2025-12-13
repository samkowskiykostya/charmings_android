#!/bin/bash
set -e

PORT=5500
DIR="app/build/outputs/apk/release"

cd "$DIR"

# Get local Wi-Fi/LAN IP (macOS first, then Linux fallbacks)
get_lan_ip() {
  # macOS Wi-Fi is usually en0
  local ip
  ip="$(ipconfig getifaddr en0 2>/dev/null || true)"
  if [[ -n "$ip" ]]; then
    echo "$ip"
    return
  fi

  # macOS sometimes uses en1
  ip="$(ipconfig getifaddr en1 2>/dev/null || true)"
  if [[ -n "$ip" ]]; then
    echo "$ip"
    return
  fi

  # Linux: hostname -I (first non-empty)
  ip="$(hostname -I 2>/dev/null | awk '{print $1}' || true)"
  if [[ -n "$ip" ]]; then
    echo "$ip"
    return
  fi

  # Linux: ip route (interface route)
  ip="$(ip route get 1.1.1.1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if ($i=="src") {print $(i+1); exit}}' || true)"
  if [[ -n "$ip" ]]; then
    echo "$ip"
    return
  fi

  echo ""
}

IP="$(get_lan_ip)"

echo "Serving: $(pwd)"
if [[ -n "$IP" ]]; then
  echo "URL: http://$IP:$PORT/"
else
  echo "URL: http://<your-lan-ip>:$PORT/   (couldn’t auto-detect IP)"
fi
echo "Port: $PORT"
echo ""

# Start the server (python3 preferred, fallback to python)
if command -v python3 >/dev/null 2>&1; then
  exec python3 -m http.server "$PORT"
else
  exec python -m http.server "$PORT"
fi
