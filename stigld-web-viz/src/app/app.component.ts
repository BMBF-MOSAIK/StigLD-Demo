import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit {
  dataList: any;
  topoi: any;
  msg: string;
  body: string;
  wait: number = 0;
  order: number = 0;
  constructor(private http: HttpClient) {
    this.dataList = [];

  }

  ngOnInit(): void {
    this.getData();
    // setTimeout(() => {
    //   window.location.reload();
    // }, 10000);
    
  }

  getData(): void {
    this.http
      // .get(`https://run.mocky.io/v3/e98cb9d0-4250-4957-8c8c-5b1d94596d44`)
      .get(`http://localhost:8080/sparql/json`)
      .subscribe((res) => {
        this.dataList = res;
        // console.log(this.dataList);
      });
  }

  orderEvent(){

    this.http.post(`http://localhost:8080/sparql/update`, `PREFIX ex:<http://example.org/>
    PREFIX pos: <http://example.org/property/position#>
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
    PREFIX st:  <http://example.org/stigld/>
    Prefix point: <http://gridPoint/>
    PREFIX stigFN: <http://www.dfki.de/func#>
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    
    INSERT{?uri a ex:Order ; ex:created ?created }
    WHERE{
      BIND(UUID() as ?uri) BIND (NOW() as ?created)
    };

    INSERT {
    ?taskURI  a        ex:WorkstationTask ;  ex:startTime  ?createTime;  ex:endTime ?endTime; ex:order ?order .
    ?machine ex:queue ?taskURI.
    ?stigmaURI a     ex:NegFeedback , st:Stigma ; st:created  ?createTime ;
      st:decayRate  "0.5"^^xsd:double ; st:level      "100.0"^^xsd:double .
    
    ?topos st:carries ?stigmaURI.
    }
    WHERE{
      {SELECT DISTINCT ?machine ?topos ?taskURI ?stigmaURI ?s WHERE {
        BIND (BNODE() AS ?taskURI)  BIND (BNODE() AS ?stigmaURI)
        ?machine a ex:ProductionArtifact;	ex:located ?topos;
        ex:outputPort [ a ex:Port; ex:located ?outputPort; ex:capacity ?capacity ] .
    
        OPTIONAL {SELECT (COUNT(?done) as ?waiting) ?outputPort {
          ?done a ex:Product ; ex:located ?outputPort . } GROUP BY ?outputPort }
    
        OPTIONAL {SELECT (COUNT(?sched) as ?scheduled) ?machine WHERE {
          ?machine ex:queue ?sched.	} GROUP BY ?machine }
    
        OPTIONAL { ?topos st:carries [ a ex:NegFeedback ; st:level ?lvl] }
        BIND(IF(bound(?lvl), ?lvl, 0) as ?l)
        BIND(IF(bound(?waiting), ?waiting, 0) as ?w)
        BIND(IF(bound(?scheduled), ?scheduled, 0) as ?s)
        FILTER(?w+?s < ?capacity)
    
        } ORDER BY ASC (?l) LIMIT 1}
    
      { SELECT ?order WHERE {
        ?order a ex:Order ; ex:created ?created .
        FILTER NOT EXISTS { ?otherTask a ex:WorkstationTask ; ex:order ?order .}
      } ORDER BY (?created) LIMIT 1}
    
      BIND((NOW() + "PT10S"^^xsd:duration * ?s) as ?createTime)
      BIND(?createTime + "PT5S"^^xsd:duration  as ?endTime)
    }
    `)        
    .subscribe((res) => {
      //this.dataList = res;
       console.log("Hi");
    });
    console.log("Button Clicked");
    location.reload(true);
  }

  

  getRgbColor(obj): string 
  {
    if (obj) 
	  {
      if (obj.machine)
      { 
        this.wait = this.wait+obj.machine.waiting;
        this.order = this.order+obj.machine.orders;
        // console.log("Wait: "+ obj.machine.waiting)
        // console.log("Order: "+ obj.machine.orders)
        if(obj.negFeedback){          
          return `rgb(255,0,0, ${obj.negFeedback/100})`;
        }
        else{
          // console.log("else negFeedback");
          return `rgb(169,169,169)`;  // if machine with no neg feedback, cell is grey
        }
      }
      else if (obj.transporter)
      {
        if (obj.transporter.timeToPickup) 
        {                     
          return `rgb(135,206,250)`;   
        }
        else if(obj.transporter.timeToMove)
        {
          return `rgb(255,242,179)`
        }
        else
          return `rgb(255,165,0)`;        //if transporter wth no time to pick up, cell is orange
      }
      else if (obj.outputPort)
      {
        return `rgb(85, 102, 0)`;
      }
      else if (obj.transportStigma) 
      {
        if(obj.transportStigma >0 && obj.transportStigma < 0.0001)
          return `rgb(0,128,0,0.2)`;   //add green as diffusion trace  color
        else if(obj.transportStigma>0.00001 && obj.transportStigma < 0.0001)
          return `rgb(0,128,0,0.4)`;   //add green as diffusion trace color
        else if(obj.transportStigma>0.0001 && obj.transportStigma < 0.001)
          return `rgb(0,128,0,0.6)`;   //add green as diffusion trace color  
        else if(obj.transportStigma>0.001 && obj.transportStigma < 0.01)
          return `rgb(0,128,0,0.8)`;   //add green as diffusion trace color
        else if(obj.transportStigma>0.01 && obj.transportStigma < 0.1)
          return `rgb(0,128,0,0.9)`;   //add green as diffusion trace color
        else
          return `rgb(0,128,0, 1)`;   //add green as diffusion trace color          
      } 
      else if (obj.diffusionTrace) 
      { 
        if((obj.diffusionTrace==0||obj.diffusionTrace>0) && obj.diffusionTrace < 0.00001)
          return `rgb(0,128,0,0.2)`;   //add green as diffusion trace  color
        else if(obj.diffusionTrace>0.00001 && obj.diffusionTrace < 0.0001)
          return `rgb(0,128,0,0.35)`;   //add green as diffusion trace color
        else if(obj.diffusionTrace>0.0001 && obj.diffusionTrace < 0.001)
          return `rgb(0,128,0,0.55)`;   //add green as diffusion trace color  
        else if(obj.diffusionTrace>0.001 && obj.diffusionTrace < 0.01)
          return `rgb(0,128,0,0.8)`;   //add green as diffusion trace color
        else if(obj.diffusionTrace>0.01 && obj.diffusionTrace < 0.1)
          return `rgb(0,128,0,0.9)`;   //add green as diffusion trace color
        else
          return `rgb(0,128,0, 1)`;   //add green as diffusion trace color    
        
      }
      else
        return `rgb(255,255,255)`;
    }
  }

  // getNumberOfOrders(): number
  // {
  //   console.log("Orders: "+this.order);
  //   return this.order;
  // }
  // getNumberOfWait(): number
  // {
  //   // console.log("Wait: "+this.wait);
  //   return this.wait;
  // }
}


