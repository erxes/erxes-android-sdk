# Security Policy

## Reporting a vulnerability

If you discover a security vulnerability in the erxes Android Messenger SDK, please
report it privately — **do not open a public GitHub issue**.

Email **info@erxes.io** with:

- a description of the issue and its potential impact,
- steps to reproduce (proof-of-concept if possible),
- the SDK version and affected component.

You can expect an acknowledgement of your report, and we will keep you informed as we
investigate and work on a fix. Please give us a reasonable window to address the issue
before any public disclosure.

## Supported versions

This SDK is pre-1.0 and under active development. Security fixes are applied to the
latest released version on `main`.

## Scope

This SDK embeds a customer messenger that talks to an erxes backend over GraphQL and
WebSocket. Relevant areas include the connect handshake, message/file transport, and the
persistence of customer/visitor identity. Note that the SDK does not hard-code any
endpoint or integration id — those are supplied by the host application.
