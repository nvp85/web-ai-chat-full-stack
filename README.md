# AI Chat Uniform Web Interface

## Description

The project is a full-stack web application built as part of the LaunchCode coding bootcamp to practice React and the Spring framework. 
It offers a unified web interface for interacting with LLMs from different AI providers. Currently, the app supports two LMMs: OpenAI gpt-4o-mini and Google's Gemini flash, so users can experiment without switching tools.

Core features include:
- Token-based authentication (no refresh tokens for simplicity).
- Create and manage chats: start new conversations, rename or delete existing ones.
- Automatically generated chat titles: the app attempts to generate a title from the first message, with a fallback if the API call fails.

## Tchnologies

Frontend: React, React Router

Backend: the Spring framework (Spring Boot, Spring Web, Spring Security, Spring Data) 

Database: MySQL

Authentication/Authorization: [security-jwt library](https://github.com/bratkartoffel/security-jwt) 

## Installation

Prerequisites: Node.js, MySQL database server, Java 21+ SDK, Maven, OpenAI and Google API keys.

To run this project locally implement the following steps:

* Clone the repository
```
git clone https://github.com/nvp85/web-ai-chat-full-stack.git
```
* In the frontend folder run
```
npm install
npm run dev
```
The frontend will run on http://localhost:5173.
* Create a database schema named ai-chat
* Set environment variables for your DB connection: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS
* Set environment variables for your OpenAI and Google API keys: GOOGLE_API_KEY and OPENAI_API_KEY
```
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ai-chat
export DB_USER=root
export DB_PASS=your-password
export GOOGLE_API_KEY=your-key
export OPENAI_API_KEY=your-key
```
* In the backend folder run
```
mvn spring-boot:run
```
The backend will run on http://localhost:8080.

Create a new user via UI.
## Wireframes

[Wireframes on Google Drive](https://drive.google.com/file/d/1aWjO8r3u8KOvoDtXZWIhaTN33A3SepHR/view?usp=sharing)

## ERDs

![ERDs on Google Drive](https://drive.google.com/uc?id=1HPdFyrj9XHg-c_zeY-l42cM2erZVPMZb)

## Unsolved problems or future features

- Add one more LLM
- A chat list search
- Refresh tokens, so a user doesn't have to re-log in
- Web UI color schemes

