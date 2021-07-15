# Override this parameter when invoking make CLI:
#  $ make package IMAGE_REPOSITORY=myregistry/myrepo
IMAGE_REPOSITORY = index.docker.io/alexandreroman

FRONTEND_GENERATED_CONTENT = frontend/js/vue.min.js frontend/js/director.min.js frontend/js/axios.min.js frontend/js/axios.min.map frontend/css/styles.css

all: package

clean:
	-rm -rf frontend/node_modules ${FRONTEND_GENERATED_CONTENT} frontend.zip backend/target

package: package-frontend package-backend

package-frontend: build-frontend
	pack build -p frontend ${IMAGE_REPOSITORY}/k8s-todo-frontend

package-backend: build-backend
	cd backend && ./mvnw -DskipTests spring-boot:build-image -Dspring-boot.build-image.imageName=${IMAGE_REPOSITORY}/k8s-todo-backend

# Use this task to generate a zip file you may want to use with kpack / TBS
# when building an image.
zip-frontend: build-frontend
	zip -r frontend.zip frontend/index.html frontend/js frontend/css \
		frontend/nginx.conf frontend/mime.types frontend/project.toml

build-frontend: ${FRONTEND_GENERATED_CONTENT}

${FRONTEND_GENERATED_CONTENT}:
	cd frontend && NODE_ENV=production npm i
	mkdir -p frontend/{js,css}
	cp frontend/node_modules/{director/build/director.min.js,vue/dist/vue.min.js,axios/dist/axios.min.js,axios/dist/axios.min.map} frontend/js
	cp frontend/node_modules/todomvc-app-css/index.css frontend/css/styles.css

build-backend: backend/target/k8s-todo-backend.jar

backend/target/k8s-todo-backend.jar:
	cd backend && ./mvnw package

run-frontend: build-frontend
	cd frontend && python3 -m http.server
	docker run --rm -p 9000:9000 -e PORT=9000 ${IMAGE_REPOSITORY}/k8s-todo-frontend

run-backend: build-backend
	java -jar backend/target/k8s-todo-backend.jar
