# StigLD-Demo

StigLD is a domain model that can be used to create a stigmergic linked system. This demonstration shows a Make-to-Order fulfilment process from the production domain. Make-to-Order isa production approach in which manufacturing starts only after a customer’s order is received. The demonstration consists of a shop floor which is represented by a discrete grid. Each cell is a location of the shop floor which can contain one of three things: produciton machines, output slots attached to individual production machines and transporters. 

Production machines manufacture some finished product and place them in their output slots. Output slots have a maximum capacity, which on being filled, would need to be emptied by transporters which pick up the finished products. Only when the output slots are not filled to capacity, can a machine continue manufacturing products. The shop floor can keep receiving new orders, and the aim of the demonstration is to coordinate the fulfilment of these orders such that all orders are fulfilled with the least number of updates, least number of movements from the transporters and that all the production machines are loaded uniformly. Another important optimisation metric is that a finished product should spend as little time in the output slot as possible, to ensure a smooth and uninterrupted production process.

# Behaviour Description
The cell types and their description are given on the left hand side of the visualisation tab. The machines tab with labelled "M" can have two colours. Grey when it is idle and has not been used in the recent past, and Red when it is either currently being used, or has been used in the recent past. The intensity of the Red colour fades as time passes. This fading is driven by the linear decay of the negative feedback markers placed on machines, which inhibits the use of a busy machine if the other machines are relatively less loaded. 

The cells containing transporters are labelled with "T", and have three different colours depending on what the transporter is doing at that time. Orange when idle, yellow when it is moving to some adjacent cell and blue when it is picking up finished products at output slots, labelled "O". 

The green cells signify diffusion traces, that is, the traces that finished products emit when in the output slot. Again, the intensity of the green signifies the strength of the diffusion trace at a particular cell, being highest at the output slots. The diffusion traces do not decay linearly in-place, but diffuse and spread over the entireshop floor. This results in a diffusion gradient, with intensity of diffusion markers increasing towards a finished product. Transporters follow this gradient to pickup products from output slots.


# Setup
There are two ways to set up the stigLD demonstration. For a quick and simple setup, we recommend that you follow option 2.
## 1. Build from source code

This project has four components:
1. The stigFN server: implements the custom SPARQL functions for evolution of Stigmata.
2. Medium server: Injects evolution queries before the user queries to maintain consistency of world view before responding to a user query.
3. Web-viz: Visualises the dynamics of the demonstrations on a browser window
4. Queries&models: SPARQL queries implement the agent behaviour, while the model files create a representation of the shop floor in RDF.

Following the instructions from the readme of individual folders, and then open http://localhost:4200 on a (preferably) chromium based browser.

## Requirements
1. Java JDK 15
2. Any Java IDE, like Apache Netbeans or Intellij Idea
3. Maven 3.3.9 or higher
4. Angular CLI 11.0.5 or higher
5. Postman HTTP client or similar


## 2. Use docker containers provided:

Docker containers can be pulled from dockerhub (heads up: large image files may take several minutes to download). Installation of Docker is a prerequisite. Instructions can be found [here](https://docs.docker.com/desktop/). Once installed, open cmd and do the following: 
1. ```docker pull melzchelli/stigld-fusekiserver:latest```
2. ```docker pull melzchelli/stigld-agentrunner:latest```
3. ```docker pull melzchelli/stigld-mediumserver:latest```
4. ```docker pull melzchelli/stigld-visualise:latest```

Once this is done, cd into the directory containing docker-compose.yml and run:
```
docker-compose up
```

Once all the services are running, open http://localhost:4200. There are some production orders pre-loaded for demonstration purposes. New orders can be sent using the "Send Orders" button on the visualisation page.

# Acknowledgements
This work has been supported by the German Federal Ministry for Education and Research (BMBF) as part of the MOSAIK project (grant no. 01IS18070-C).
