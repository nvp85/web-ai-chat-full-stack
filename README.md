# AI Chat Uniform Web Interface

## Description

The project is a full-stack web application built as part of the LaunchCode coding bootcamp to practice React and the Spring framework. 
It offers a unified web interface for interacting with LLMs from different AI providers. Currently, the app supports two LMMs: OpenAI gpt-4o-mini and Google's Gemini flash, so users can experiment without switching tools.

Core features include:

Token-based authentication (no refresh tokens for simplicity).

Create and manage chats: start new conversations, rename or delete existing ones.

Automatically generated chat titles: the app attempts to generate a title from the first message, with a fallback if the API call fails.

## Tchnologies

Frontend: React, React Router

Backend: the Spring framework (Spring Boot, Spring Web, Spring Security, Spring Data) 

Database: MySQL

Authentication/Authorization: [security-jwt library](https://github.com/bratkartoffel/security-jwt) 

## Installation

## Wireframes

[Wireframes on Google Drive](https://drive.google.com/file/d/1aWjO8r3u8KOvoDtXZWIhaTN33A3SepHR/view?usp=sharing)

## ERDs

![ERDs on Google Drive](https://drive.google.com/file/d/1HPdFyrj9XHg-c_zeY-l42cM2erZVPMZb/view?usp=drive_link)

## Unsolved problems or future features

- A chat list filter by LLM
- A chat list search
- Refresh tokens, so a user doesn't have to re-log in
- Web UI color schemes

