# StigLD-Demo

StigLD is a domain model that can be used to create a stigmergic linked system. This demonstration shows a Make-to-Order fulfilment process from the production domain. Make-to-Order isa production approach in which manufacturing starts only after a customer’s order is received. The demonstration consists of a shop floor which is represented by a discrete grid. Each cell is a location of the shop floor which can contain one of three things: produciton machines, output slots attached to individual production machines and transporters. 

Production machines manufacture some finished product and place them in their output slots. Output slots have a maximum capacity, which on being filled, would need to be emptied by transporters which pick up the finished products. Only when the output slots are not filled to capacity, can a machine continue manufacturing products. The shop floor can keep receiving new orders, and the aim of the demonstration is to coordinate the fulfilment of these orders such that all orders are fulfilled with the least number of updates, least number of movements from the transporters and that all the production machines are loaded uniformly. Another important optimisation metric is that a finished product should spend as little time in the output slot as possible, to ensure a smooth and uninterrupted production process.


# Components

This project has four components:
1. The stigFN server: implements the custom SPARQL functions for evolution of Stigmata.
2. Medium server: Injects evolution queries before the user queries to maintain consistency of world view before responding to a user query.
3. Web-viz: Visualises the dynamics of the demonstrations on a browser window
4. Queries&models: SPARQL queries implement the agent behaviour, while the model files create a representation of the shop floor in RDF.

Following the instructions from the readme of individual folders, and then open http://localhost:4200 on a (preferably) chromium based browser.

# Requirements
1. Java JDK 15
2. Any Java IDE, like Apache Netbeans or Intellij Idea
3. Maven 3.3.9 or higher
4. Angular CLI 11.0.5 or higher
5. Postman HTTP client or similar


# Acknowledgements
This work has been supported by the German Federal Ministry for Education and Research (BMBF) as part of the MOSAIK project (grant no. 01IS18070-C).
