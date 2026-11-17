#!/usr/bin/env python3
"""Minimal RCON client (stdlib only), used by the P4 S4a fixture check.

    python3 scripts/rcon.py "exnihilo dump sieve"
"""

from __future__ import annotations

import argparse
import socket
import struct
import sys


def packet(request_id: int, packet_type: int, body: str) -> bytes:
    data = struct.pack("<ii", request_id, packet_type) + body.encode("utf-8") + b"\x00\x00"
    return struct.pack("<i", len(data)) + data


def read(sock: socket.socket) -> str:
    raw = b""
    while len(raw) < 4:
        chunk = sock.recv(4 - len(raw))
        if not chunk:
            raise ConnectionError("connection closed")
        raw += chunk
    length = struct.unpack("<i", raw)[0]
    data = b""
    while len(data) < length:
        chunk = sock.recv(length - len(data))
        if not chunk:
            raise ConnectionError("connection closed")
        data += chunk
    return data[8:-2].decode("utf-8", "replace")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("command")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=25575)
    parser.add_argument("--password", default="eni")
    args = parser.parse_args()

    with socket.create_connection((args.host, args.port), timeout=10) as sock:
        sock.sendall(packet(1, 3, args.password))
        read(sock)
        sock.sendall(packet(2, 2, args.command))
        # A large response (e.g. /exnihilo dump all) arrives as several packets.
        chunks = [read(sock)]
        sock.settimeout(0.4)
        while True:
            try:
                chunks.append(read(sock))
            except (TimeoutError, socket.timeout):
                break
        sys.stdout.write("".join(chunks))
    return 0


if __name__ == "__main__":
    sys.exit(main())
