# Based on https://mherman.org/blog/dockerizing-an-angular-app/

#############
### build ###
#############

# base image
FROM node:12.2.0 as build

# install chrome for protractor tests
RUN set -xe; \
    wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -; \
    sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list'; \
    apt-get update && apt-get install -yq google-chrome-stable

# set working directory
WORKDIR /app

# RUN rm -rf /node_modules

# RUN npm install

# add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

# RUN npm update


# install angular CLI
RUN npm install --unsafe-perm -g @angular/cli@11.0.5

# install and cache app dependencies
COPY package.json /app/package.json
RUN npm install --unsafe-perm

# This needs to be done to circumvent some weird dependecy conflicts between GoogleMapsAPI and agular material libs
# RUN npm uninstall @agm/core -s \
# 	&& npm uninstall @angular/core -s \
# 	&& npm install @agm/core -s \
# 	&& npm install @angular/core -set \


# add app
COPY . /app

# run tests
# RUN ng test --watch=false
# RUN ng e2e --port 4202

# generate build
RUN ng build --output-path=dist

############
### prod ###
############

# base image
FROM nginx:1.21-alpine

# remove default nginx website
RUN rm -rf /usr/share/nginx/html/* && \
    mkdir -p /etc/nginx/logs

COPY nginx.conf /etc/nginx/nginx.conf

# copy artifact build from the 'build environment'
COPY --from=build /app/dist /usr/share/nginx/html

ENV ENV_CONFIG_FILE=/usr/share/nginx/html/env.js

COPY entrypoint.sh /usr/local/bin
RUN chmod +x /usr/local/bin/entrypoint.sh

# expose port 80
EXPOSE 4200

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
# run nginx
CMD ["nginx", "-g", "daemon off;"]
