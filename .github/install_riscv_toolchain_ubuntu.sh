#!/usr/bin/env bash

set -euo pipefail

INSTALL_PATH=${1:-${PWD}}
VERSION=${2:-riscv64-unknown-elf-gcc-8.3.0-2019.08.0-x86_64-linux-ubuntu14}

SIFIVE_STATIC_URL=https://static.dev.sifive.com/dev-tools

GCC_ARCHIVE_PATH=${VERSION}.tar.gz

wget -O - ${SIFIVE_STATIC_URL}/${GCC_ARCHIVE_PATH} | tar -xz -C ${INSTALL_PATH}
