'use strict';

var showSolve = false;
var showLog = false;

document.getElementById("btnRegister").addEventListener("click", function(){
  var myMessage = compressMyMessage("register");
  log("Registration requested...");
  connection.send(myMessage);
});

document.getElementById("btnShowSolve").addEventListener("click", function(){
  var that = document.getElementById("btnShowSolve");
  if (showSolve === true) {
    that.setAttribute('class','pure-button button-error');
    showSolve = false;
  } else {
    that.setAttribute('class','pure-button button-success');
    showSolve = true;
  }
});

document.getElementById("btnShowLog").addEventListener("click", function(){
  var that = document.getElementById("btnShowLog");
  if (showLog === true) {
    that.setAttribute('class','pure-button button-error');
    showLog = false;
  } else {
    that.setAttribute('class','pure-button button-success');
    showLog = true;
  }
});

document.getElementById("btnUnregister").addEventListener("click", function(){
  var myMessage = compressMyMessage("unregister");
  log("Unregister");
  connection.send(myMessage);
});

document.getElementById("btnPing").addEventListener("click", function(){
  var myMessage = compressMyMessage("ping");
  log("Pong requested...");
  connection.send(myMessage);

});

document.getElementById("btnSolve").addEventListener("click", function(){
  var txt = document.getElementById("sudokuSize").value;
  var myMessage = compressMyMessage("solve:" + getJSONSudoku());
  log("Solve requested > " + ab2str(myMessage));
  connection.send(myMessage);
});

document.getElementById("btnGenerate").addEventListener("click", function(){
  var txt = document.getElementById("sudokuSize").value;
  var txt2 = document.getElementById("sudokuDiff").value;
  var myMessage = compressMyMessage("generate:" + txt + ":" + txt2);
  log("Generate requested > " + ab2str(myMessage));
  connection.send(myMessage);
});

document.getElementById("btnRndSudoku").addEventListener("click", function(){

  for(var i = 0; i<sudokuSize; i++) {
    for(var j = 0; j<sudokuSize; j++) {
      var txt = document.getElementById(i+"_"+j);
      txt.value = Math.ceil(Math.random() * sudokuSize);
    }
  }

  log("Sudoku randomized");
  console.log(getJSONSudoku());

});

document.getElementById("btnCreateSudoku").addEventListener("click", function(){

  var txt = document.getElementById("sudokuSize");
  createSudoku(txt.value);
  sudokuSize = txt.value;

  log("Sudoku created");

});

// Assuming the Webserver runs on the same machine as the MyCamelServant application
var hostname = window.location.hostname;
var connectionString = "ws://"+hostname+":9292/myUri";
var sudokuSize;

function str2ab(str) {
  var buf = new ArrayBuffer(str.length*2); // 2 bytes for each char
  var bufView = new Uint8Array(buf);
  for (var i=0, strLen=str.length; i<strLen; i++) {
    bufView[i] = str.charCodeAt(i);
  };
  return buf;
};

function ab2str(buf) {
  return String.fromCharCode.apply(null, new Uint8Array(buf));
};

function compressMyMessage(message) {
  var arrbuf = str2ab(message);
  var compressed = SnappyJS.compress(arrbuf);
  return compressed;
};

function createSudoku(n) {
  var div = document.getElementById("sudoku");

  while (div.firstChild) {
    div.removeChild(div.firstChild);
  }

  div.setAttribute('class','pure-form');
  for(var i = 0; i<n; i++) {
    for(var j = 0; j<n; j++) {
      var elem = document.createElement('input');
      elem.id = i + "_" + j;
      var ziffern = Math.ceil(Math.log10(n+1));
      elem.size = 1;
      elem.maxLength = ziffern;
      elem.setAttribute('class','pure-input-rounded');

      if(Math.floor(i/Math.sqrt(n))%2 == 0 ^ Math.floor(j/Math.sqrt(n))%2 == 0) {
        elem.setAttribute('style','text-align: center; background-color : #d1d1d1;');
      } else {
        elem.setAttribute('style','text-align: center;');
      }


      div.appendChild(elem);
    };
    div.appendChild(document.createElement('br'));
  };
};

function getJSONSudoku() {

  var myString = "[";

  for(var i = 0; i<sudokuSize; i++) {

    myString += "[";
    for(var j = 0; j<sudokuSize; j++) {

      myString += document.getElementById(i+"_"+j).value;
      if(j < sudokuSize -1) {
        myString += ",";
      }

    };
    myString += "]";
    if(i < sudokuSize -1) {
      myString += ",";
    }
  };

  myString += "]";
  return myString;

};

function setSudokuArr(arr) {

  log("habe Sudoku erhalten");
  // ToDo: Parse display JSON array and set sudoku objects.
  var myArr = arr.toString().split(",");
  myArr[0] = myArr[0].replace("[","");
  myArr[myArr.length - 1] = myArr[myArr.length - 1].replace("]","");


  var sz = Math.sqrt(myArr.length);
  log("length:" + myArr.length + " sqrt:" + sz);

  log("Erstelle Sudoku");
  createSudoku(sz);

  var cnt = 0;

  for(var i = 0; i<sz; i++) {
    for(var j = 0; j<sz; j++) {

      var txt = document.getElementById(i+"_"+j);
      if(myArr[cnt] == 0) {
        txt.value = "";
        txt.readOnly = false;

        if(Math.floor(i/Math.sqrt(sz))%2 == 0 ^ Math.floor(j/Math.sqrt(sz))%2 == 0) {
          txt.setAttribute('style','text-align: center; background-color : #55ff55;');
        } else {
          txt.setAttribute('style','text-align: center; background-color : #55ff55;');
        }

      } else {
        txt.value = myArr[cnt];
        txt.readOnly = true;
      }
      cnt++;

    };
  };

  log("Now I'm happy :)))))");

};

var connection = new WebSocket(connectionString);
connection.binaryType = "arraybuffer";

connection.onopen = function () {

};

connection.onerror = function (error) {
  log('WebSocket Error ' + error);
};

var myNames = [];

connection.onmessage = function (e) {
  var decompressed = SnappyJS.uncompress(e.data);
  var decomp_str = ab2str(decompressed);
  log('DEBUG > Server: ' + decomp_str);

  // parse incoming messages

  var json_parsed = JSON.parse(decomp_str);
  var hash = hashFnv32a(json_parsed.sender,true,0);
  console.log("Hash: " + hash);

  if (myNames[hash] === undefined) {
    myNames[hash] = new Array();
    myNames[hash]["sender"] = json_parsed.sender;
    myNames[hash]["prefix"] = json_parsed.sender.substring(0,13);
  };

  var arr = chart.yDomain();
  arr[arr.length] = hash + ":" + myNames[hash]["prefix"];
  chart.yDomain(arr);

  // create randomized timestamp for this category data item
  var now = new Date(new Date().getTime());
  // create new data item
  var doSimple = false;
  var obj = {
    // complex data item; four attributes (type, color, opacity and size) are changing dynamically with each iteration (as an example)
    time: now,
    color: "rgb(128, 0, 0)",
    opacity: Math.max(Math.random(), 0.3),
    category: hash + ":" + myNames[hash]["prefix"],
    //type: shapes[Math.round(Math.random() * (shapes.length - 1))], // the module currently doesn't support dynamically changed svg types (need to add key function to data, or method to dynamically replace svg object – tbd)
    type: "circle",
    size: 15,
  };

  // send the datum to the chart

  switch(json_parsed.instruction) {
    case "solved:one":
    if(showSolve === true) {
      setSudokuArr(json_parsed.sudoku);
    }
    obj.type = "rect";
    obj.color = "rgb(0, 96, 0)";
    obj.size = 20;
    break;

    case "solved:many":
    if(showSolve === true) {
      setSudokuArr(json_parsed.sudoku);
    }
    obj.type = "rect";
    obj.color = "rgb(0, 228, 0)";
    obj.size = 20;
    break;

    case "solve":
    if(showSolve === true) {
      setSudokuArr(json_parsed.sudoku);
    }

    var count = 0;
    for(var i = 0; i < json_parsed.sudoku.length; ++i){
      if(json_parsed.sudoku[i] == 0) {
        count++;
      };
    };

    var MyCount2 = count / json_parsed.sudoku.length;

    obj.type = "rect";
    obj.color = "rgb(0, 96, 0)";
    obj.size = 10 + Math.floor(MyCount2*30);


    break;

    case "display":
    setSudokuArr(json_parsed.sudoku);
    obj.type = "circle";
    obj.color = "rgb(255, 0, 0)";
    obj.size = 30;
    break;


    default:
    obj.color = "rgb(0, 64, 228)";
    obj.size = 10;

    break;

  }

  chart.datum(obj);

};

function log(text) {

  if(showLog === true) {

    var datum = new Date();

    var p = document.getElementById("myConsole");
    var content = document.createTextNode(datum.toLocaleString('de-DE') + " : " + text);
    var br = document.createElement('br');
    p.appendChild(content);
    p.appendChild(br);

    if(p.children.length > 15) {
      p.removeChild(p.firstChild);
      p.removeChild(p.firstChild);
    };
  };
};

/**
* Calculate a 32 bit FNV-1a hash
* Found here: https://gist.github.com/vaiorabbit/5657561
* Ref.: http://isthe.com/chongo/tech/comp/fnv/
*
* @param {string} str the input value
* @param {boolean} [asString=false] set to true to return the hash value as
*     8-digit hex string instead of an integer
* @param {integer} [seed] optionally pass the hash of the previous chunk
* @returns {integer | string}
*/
function hashFnv32a(str, asString, seed) {
  /*jshint bitwise:false */
  var i, l,
  hval = (seed === undefined) ? 0x811c9dc5 : seed;

  for (i = 0, l = str.length; i < l; i++) {
    hval ^= str.charCodeAt(i);
    hval += (hval << 1) + (hval << 4) + (hval << 7) + (hval << 8) + (hval << 24);
  }
  if( asString ){
    // Convert to 8 digit hex string
    return ("0000000" + (hval >>> 0).toString(16)).substr(-8);
  }
  return hval >>> 0;
}

// create the real time chart
var chart = realTimeChartMulti()
.title("Network Events")
.yTitle("Entitys")
.xTitle("Time")
.yDomain([]) // initial y domain (note array)
.border(true)
.width(900)
.height(350);

// invoke the chart
var chartDiv = d3.select("#viewDiv").append("div")
.attr("id", "chartDiv")
.call(chart);
var tX = 5; // time constant, multiple of one second
var meanMs = 1000 * tX, // milliseconds
dev = 200 * tX; // std dev
var timeScale = d3.scale.linear()
.domain([300 * tX, 1700 * tX])
.range([300 * tX, 1700 * tX])
.clamp(true);
var normal = d3.random.normal(meanMs, dev);
var color = d3.scale.category10();
var d = -1;
var shapes = ["rect", "circle"];
var timeout = 0;

log("GUI 0.1 Successfully loaded!");
