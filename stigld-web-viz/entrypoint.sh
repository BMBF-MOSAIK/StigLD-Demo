#!/bin/sh

set -e
export LC_ALL=C
unset CDPATH

message() {
    echo >&2 "[entrypoint.sh] $*"
}

info() {
    message "info: $*"
}

error() {
    echo >&2 "* [entrypoint.sh] Error: $*"
}

fatal() {
    error "$@"
    exit 1
}

message "info: EUID=$(id -u) args: $*"

usage() {
    echo "Entrypoint Script"
    echo
    echo ""
    echo "$0 [options]"
    echo "options:"
    echo "      --print-env            Display environment"
    echo "      --help-entrypoint      Display this help and exit"
}

while [ $# -gt 0 ]; do
    case "$1" in
        --help-entrypoint)
            usage
            exit
            ;;
        --print-env)
            env >&2
            shift
            ;;
        --)
            shift
            break
            ;;
        -*)
            break
            ;;
        *)
            break
            ;;
    esac
done

CFG_API_SERVER=

if [ -n "$API_SERVER" ]; then
  CFG_API_SERVER=$API_SERVER
elif [ -n "$MEDIUM_SERVER" ]; then
  CFG_API_SERVER=$MEDIUM_SERVER
fi

ENV_CONFIG_FILE=${ENV_CONFIG_FILE:-/app/src/env.js}

if [ -n "$CFG_API_SERVER" ]; then
  echo "MEDIUM_SERVER / API_SERVER = $CFG_API_SERVER"
  echo "Writing configuration to $ENV_CONFIG_FILE"
  cat > "$ENV_CONFIG_FILE" <<EOF
(function (window) {
  window.__env = window.__env || {};

  // API url
  window.__env.apiUrl = "$CFG_API_SERVER";

  // Whether or not to enable debug mode
  // Setting this to false will disable console output
  window.__env.enableDebug = true;
}(this));
EOF
fi

set -xe
exec "$@"
