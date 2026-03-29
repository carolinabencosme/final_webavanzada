.PHONY: build up down logs clean ps restart

build:
	cd services && mvn clean package -DskipTests

up:
	docker-compose up -d --build

down:
	docker-compose down

logs:
	docker-compose logs -f

ps:
	docker-compose ps

clean:
	docker-compose down -v
	cd services && mvn clean

restart:
	docker-compose restart
