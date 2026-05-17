SHELL := /bin/bash

OLLAMA_MODEL ?= qwen2.5:3b-instruct
COMPOSE      := docker compose

.PHONY: help up down reset logs ps restart rebuild backend frontend mysql ollama \
        pull-model open clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
	  | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

up: ## Build and start the full stack (mysql, backend, ollama, frontend)
	$(COMPOSE) up -d --build
	@echo
	@echo "  Frontend: http://localhost:4000"
	@echo "  Backend:  http://localhost:9090"
	@echo "  Ollama:   http://localhost:11434"
	@echo
	@echo "  If this is the first run, pull the AI model: make pull-model"

down: ## Stop containers (keeps volumes/data)
	$(COMPOSE) down

reset: ## Wipe data (volumes) and start fresh
	$(COMPOSE) down -v
	$(COMPOSE) up -d --build

restart: ## Restart all services
	$(COMPOSE) restart

rebuild: ## Rebuild and restart only the Spring backend
	$(COMPOSE) up -d --build --no-deps backend

logs: ## Tail logs from all services
	$(COMPOSE) logs -f

backend: ## Tail backend logs
	$(COMPOSE) logs -f backend

frontend: ## Tail frontend (nginx) logs
	$(COMPOSE) logs -f frontend

mysql: ## Open a MySQL shell inside the db container
	$(COMPOSE) exec mysql mysql -u jlpp1TU5AL -pjlpp1TU5AL fintech_db

ollama: ## Tail ollama logs
	$(COMPOSE) logs -f ollama

ps: ## Show service status
	$(COMPOSE) ps

pull-model: ## Download the AI model used for recommendations
	$(COMPOSE) exec ollama ollama pull $(OLLAMA_MODEL)

open: ## Open the app in the default browser
	@open http://localhost:4000/register.html 2>/dev/null || \
	  xdg-open http://localhost:4000/register.html 2>/dev/null || \
	  echo "Open http://localhost:4000/register.html in your browser"

clean: ## Stop everything and remove volumes, networks and images built locally
	$(COMPOSE) down -v --rmi local
