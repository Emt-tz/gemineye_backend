package tz.co.geminey.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import tz.co.geminey.feature.auth.authRoute
import tz.co.geminey.feature.card_categories.cardCategoriesRoute
import tz.co.geminey.feature.cards.cardRoute
import tz.co.geminey.feature.compatibility.quizRoute
import tz.co.geminey.feature.images.imageRoutes
import tz.co.geminey.feature.matches.matchesRoute
import tz.co.geminey.feature.user.userRoute
import tz.co.geminey.routes.interestRoute

fun Application.configureRouting() {
    val rootDirectory = System.getProperty("user.dir")
    println("APPLICATION ROOT PATH: $rootDirectory")
    authRoute(rootDirectory)

    routing {
        imageRoutes(rootDirectory)
        cardRoute()
        authenticate("auth-jwt") {
            matchesRoute()
            cardCategoriesRoute()
            interestRoute()
            userRoute(rootDirectory)
            quizRoute()
        }
    }
}


/*
server {
    listen 80;
    listen [::]:80;

    server_name 51.20.82.169;

    root /var/www/html/gemineye/public/;
    index index.html index.php;

    location / {
        try_files $uri $uri/ /index.php?$args;
    }

    location ~ .php$ {
	root /var/www/html/gemineye/public/;
        try_files $uri =404;
        fastcgi_split_path_info ^(.+.php)(/.+)$;
        fastcgi_pass unix:/run/php/php8.1-fpm.sock;
        fastcgi_index index.php;
	fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
        include fastcgi_params;
    }
}*/