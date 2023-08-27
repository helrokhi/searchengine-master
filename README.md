Локальный поисковый движок по сайту searchengine-master.

Поисковый движок представляет собой Spring-приложение (JAR-файл, запускаемый на любом сервере или компьютере), работающее с локально установленной базой данных MySQL, имеет простой
веб-интерфейс и API, через который им можно управлять и получать результаты поисковой выдачи по запросу.

Принципы работы поискового движка
1. В конфигурационном файле перед запуском приложения задаются адреса сайтов, по которым движок должен осуществлять поиск.
2. Поисковый движок самостоятельно обходит все страницы заданных сайтов и индексирует их (создает так называемый индекс) так, чтобы потом находить наиболее релевантные страницы по любому поисковому запросу.
3. Пользователь присылает запрос через API движка. Запрос — это набор слов, по которым нужно найти страницы сайта.
4. Запрос определённым образом трансформируется в список слов, переведённых в базовую форму. Например, для существительных — именительный падеж, единственное число.
5. В индексе ищутся страницы, на которых встречаются все эти слова.
6. Результаты поиска сортируются и отдаются пользователю.

К проекту подключены библиотеками лемматизаторами и содержит несколько контроллеров, сервисов и репозиторий с подключением к бд MySQL.

Проект выполнен на заготовке проекта (простого приложения на Spring Boot), загруженного из репозитория. Заготовка проекта включала frontend-составляющую.

Проект соответствует технической документации, приложенной к заготовке проекта в части описания веб-интерфейса, структуры таблиц базы данных и документации по командам API.

Для работы приложения необходимо, установить на компьютер MySQL-сервер для хранения данных проиндексмированных сайтов и создать в нём пустую базу данных search_engine.
Для подключения к базе данных создайте пользователя root, который имеет доступ ко всем базам данных (создаётся при установке MySQL-сервера). 

Конфиг application.yaml в корне проекта содержит в явном виде порт, по которому будет доступно веб-приложение, а также
данные доступа к MySQL-серверу и список сайтов для индексации.

Проверка API
Для проверки API данного проекта вы можете использовать разные инструменты:
1. Postman - приложения для отправки запросов и тестирования API.
2. Любой браузер. Запустите приложение. В адресную строку введите http://localhost:8080/.