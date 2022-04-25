#step 1
FROM node:12.14.1-alpine  AS ui-build
WORKDIR /usr/src/app
COPY package*.json /usr/src/app/
RUN npm i npm@7.5.6
COPY . /usr/src/app
RUN npm run build --prod

#step 2
FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=ui-build /usr/src/app/dist/client /usr/share/nginx/html

#step 3
EXPOSE 80
