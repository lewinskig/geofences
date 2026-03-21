# Lista grzechów i uproszczeń
* Postgres: ma być zainstalowany i uruchomiony na lokalnej maszynie
* Postgres w testach: testcontainers byłoby bardziej na miejscu
* Postgres: jest w domyślnej konfiguracji - bez hasła ani uwierzytelniania - security
* Postgres: nie ma własnej schemy - wszystko jest w public
* REST: brak https/certyfikatu - security
* REST: brak autoryzacji - security
* Geofence: obsługa antymeridian - narazie ładnie działa w Europie

# Todo: 
[ ] Postgres: index tylko po czasie jest za słaby, trzeba by zmienić po trackId+czasie