#!/bin/sh
set -eu

json_escape() {
  printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'
}

validate_origin() {
  value="$1"
  name="$2"

  case "$value" in
    http://*|https://*) ;;
    *)
      echo >&2 "$name must start with http:// or https://"
      exit 1
      ;;
  esac

  case "$value" in
    *" "*|*";"*|*"{"*|*"}"*)
      echo >&2 "$name contains characters that would break nginx config."
      exit 1
      ;;
  esac
}

parse_origin() {
  origin="$1"
  scheme="${origin%%://*}"
  authority="${origin#*://}"
  authority="${authority%%/*}"

  case "$authority" in
    *:*)
      host="${authority%:*}"
      port="${authority##*:}"
      ;;
    *)
      host="$authority"
      if [ "$scheme" = "https" ]; then
        port=443
      else
        port=80
      fi
      ;;
  esac

  printf '%s\n%s\n%s\n' "$scheme" "$host" "$port"
}

validate_boolean() {
  case "$1" in
    true|false) ;;
    *)
      echo >&2 "Boolean value must be true or false"
      exit 1
      ;;
  esac
}

if [ "$#" -eq 0 ] || [ "$1" = "nginx" ]; then
  : "${BACKEND_ORIGIN:?BACKEND_ORIGIN must be set, e.g. https://api.example.com}"
  : "${PUBLIC_ORIGIN:?PUBLIC_ORIGIN must be set, e.g. https://app.example.com}"
  : "${AUTH_ISSUER:?AUTH_ISSUER must be set}"
  : "${AUTH_CLIENT_ID:?AUTH_CLIENT_ID must be set}"

  PUBLIC_ORIGIN="${PUBLIC_ORIGIN%/}"
  BACKEND_ORIGIN="${BACKEND_ORIGIN%/}"

  export PUBLIC_ORIGIN
  export BACKEND_ORIGIN
  export AUTH_ISSUER
  export AUTH_CLIENT_ID
  export AUTH_DISCOVERY_URL="${AUTH_DISCOVERY_URL:-}"
  export AUTH_SCOPE="${AUTH_SCOPE:-openid profile email groups entitlements}"
  export AUTH_REDIRECT_URI="${AUTH_REDIRECT_URI:-$PUBLIC_ORIGIN/auth/callback}"
  export AUTH_POST_LOGOUT_REDIRECT_URI="${AUTH_POST_LOGOUT_REDIRECT_URI:-$PUBLIC_ORIGIN/auth/login}"
  export AUTH_USER_CONTEXT_ENDPOINT="${AUTH_USER_CONTEXT_ENDPOINT:-/api/me}"
  export AUTH_REQUIRE_USER_CONTEXT="${AUTH_REQUIRE_USER_CONTEXT:-false}"

  validate_origin "$BACKEND_ORIGIN" "BACKEND_ORIGIN"
  validate_origin "$PUBLIC_ORIGIN" "PUBLIC_ORIGIN"
  validate_boolean "$AUTH_REQUIRE_USER_CONTEXT"

  ORIGIN_PARTS="$(parse_origin "$PUBLIC_ORIGIN")"
  PUBLIC_SCHEME="$(printf '%s\n' "$ORIGIN_PARTS" | sed -n '1p')"
  PUBLIC_HOST="$(printf '%s\n' "$ORIGIN_PARTS" | sed -n '2p')"
  PUBLIC_PORT="$(printf '%s\n' "$ORIGIN_PARTS" | sed -n '3p')"
  export PUBLIC_SCHEME
  export PUBLIC_HOST
  export PUBLIC_PORT

  mkdir -p \
    /tmp/nginx/conf.d \
    /tmp/nginx-client-body \
    /tmp/nginx-proxy \
    /tmp/nginx-fastcgi \
    /tmp/nginx-uwsgi \
    /tmp/nginx-scgi

  AUTH_ISSUER_JSON="$(json_escape "$AUTH_ISSUER")"
  AUTH_CLIENT_ID_JSON="$(json_escape "$AUTH_CLIENT_ID")"
  AUTH_DISCOVERY_URL_JSON="$(json_escape "$AUTH_DISCOVERY_URL")"
  AUTH_SCOPE_JSON="$(json_escape "$AUTH_SCOPE")"
  AUTH_REDIRECT_URI_JSON="$(json_escape "$AUTH_REDIRECT_URI")"
  AUTH_POST_LOGOUT_REDIRECT_URI_JSON="$(json_escape "$AUTH_POST_LOGOUT_REDIRECT_URI")"
  AUTH_USER_CONTEXT_ENDPOINT_JSON="$(json_escape "$AUTH_USER_CONTEXT_ENDPOINT")"

  cat > /tmp/nginx/conf.d/default.conf <<EOF
server {
  listen 8080;
  server_name _;

  root /usr/share/nginx/html;
  index index.html;

  location = /auth-config.json {
    alias /tmp/auth-config.json;
    default_type application/json;
    add_header Cache-Control "no-store";
  }

  location / {
    try_files \$uri \$uri/ /index.html;
  }

  location = /api {
    return 301 /api/;
  }

  location /api/ {
    proxy_pass $BACKEND_ORIGIN;
    proxy_http_version 1.1;
    proxy_set_header Host \$proxy_host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Host $PUBLIC_HOST;
    proxy_set_header X-Forwarded-Port $PUBLIC_PORT;
    proxy_set_header X-Forwarded-Proto $PUBLIC_SCHEME;
  }

  location = /oauth2 {
    return 301 /oauth2/;
  }

  location /oauth2/ {
    proxy_pass $BACKEND_ORIGIN;
    proxy_http_version 1.1;
    proxy_set_header Host \$proxy_host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Host $PUBLIC_HOST;
    proxy_set_header X-Forwarded-Port $PUBLIC_PORT;
    proxy_set_header X-Forwarded-Proto $PUBLIC_SCHEME;
  }

  location = /oauth {
    return 301 /oauth/;
  }

  location /oauth/ {
    proxy_pass $BACKEND_ORIGIN;
    proxy_http_version 1.1;
    proxy_set_header Host \$proxy_host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Host $PUBLIC_HOST;
    proxy_set_header X-Forwarded-Port $PUBLIC_PORT;
    proxy_set_header X-Forwarded-Proto $PUBLIC_SCHEME;
  }

  location = /login/oauth2 {
    return 301 /login/oauth2/;
  }

  location /login/oauth2/ {
    proxy_pass $BACKEND_ORIGIN;
    proxy_http_version 1.1;
    proxy_set_header Host \$proxy_host;
    proxy_set_header X-Real-IP \$remote_addr;
    proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Host $PUBLIC_HOST;
    proxy_set_header X-Forwarded-Port $PUBLIC_PORT;
    proxy_set_header X-Forwarded-Proto $PUBLIC_SCHEME;
  }
}
EOF

  cat > /tmp/auth-config.json <<EOF
{
  "issuer": "$AUTH_ISSUER_JSON",
  "discoveryUrl": "$AUTH_DISCOVERY_URL_JSON",
  "clientId": "$AUTH_CLIENT_ID_JSON",
  "redirectUri": "$AUTH_REDIRECT_URI_JSON",
  "postLogoutRedirectUri": "$AUTH_POST_LOGOUT_REDIRECT_URI_JSON",
  "scope": "$AUTH_SCOPE_JSON",
  "claimsSource": "both",
  "groupClaim": [
    "groups",
    "group"
  ],
  "entitlementClaim": [
    "entitlements",
    "entitlement"
  ],
  "permissionClaim": [
    "permissions",
    "permission",
    "authorities"
  ],
  "usernameClaim": [
    "preferred_username",
    "username",
    "sub"
  ],
  "displayNameClaim": [
    "name",
    "preferred_username"
  ],
  "emailClaim": [
    "email"
  ],
  "idClaim": [
    "sub"
  ],
  "userContextEndpoint": "$AUTH_USER_CONTEXT_ENDPOINT_JSON",
  "requireUserContext": $AUTH_REQUIRE_USER_CONTEXT,
  "storage": "localStorage",
  "defaultReturnUrl": "/",
  "clockSkewInSeconds": 60
}
EOF

  echo "Rendered Nginx runtime config under /tmp."
  if ! nginx -t -c /etc/nginx/nginx.conf; then
    echo >&2 "--- nginx debug ---"
    id >&2
    echo >&2 "--- /etc/nginx/nginx.conf ---"
    sed -n '1,200p' /etc/nginx/nginx.conf >&2
    echo >&2 "--- /tmp/nginx/conf.d/default.conf ---"
    sed -n '1,200p' /tmp/nginx/conf.d/default.conf >&2
    echo >&2 "--- /tmp/auth-config.json ---"
    sed -n '1,200p' /tmp/auth-config.json >&2
    exit 1
  fi
fi

exec "$@"
