## Создание контейнера с базой данных MySQL в Docker  

    docker run -d --name=searcheninedb -e="MYSQL_ROOT_PASSWORD=11111111" -e="MYSQL_DATABASE=search_engine" -p3306:3306 mysql
