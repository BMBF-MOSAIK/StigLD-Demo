FROM node:12.2.0

WORKDIR /app

ENV PATH /app/node_modules/.bin:$PATH

COPY package.json /app/package.json
RUN npm install
RUN npm install -g @angular/cli@11.0.5

COPY entrypoint.sh /usr/local/bin
RUN chmod +x /usr/local/bin/entrypoint.sh

COPY . /app

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]

CMD ng serve --host 0.0.0.0
