<html>
<head>
<title> JSONPath - Tests (php)</title>
</head>
<body>
<pre>
<?php
require_once('json.php');
require_once('jsonpath.php');
$json = '
[ { "o": { a: "a",
           b: "b",
           "c d": "e" 
         },
    "p": [ "$.a",
           "$[\'a\']",
           "$.\'c d\'",
           "$.*",
           "$[\'*\']" ,
           "$[*]"
         ],
  },
  { "o": [ 1, "2", 3.14, true, null ],
    "p": [ "$[0]",
           "$[4]",
           "$[*]",
	   "$[-1:]"
         ]
  },
  { "o": { points: [
             { id: "i1", x:  4, y: -5 },
             { id: "i2", x: -2, y:  2, z: 1 },
             { id: "i3", x:  8, y:  3 },
             { id: "i4", x: -6, y: -1 },
             { id: "i5", x:  0, y:  2, z: 1 },
             { id: "i6", x:  1, y:  4 }
           ]
         },
    "p": [ "$.points[1]",
           "$.points[4].x",
           "$.points[?(@[\'id\']==\'i4\')].x",
           "$.points[*].x",
           "$[\'points\'][?(@[\'x\']*@[\'x\']+@[\'y\']*@[\'y\'] > 50)].id",
           "$.points[?(@[\'z\'])].id",
           "$.points[(count(@)-1)].id"
         ]
  },
  { "o": { "menu": {
             "header": "SVG Viewer",
             "items": [
                 {"id": "Open"},
                 {"id": "OpenNew", "label": "Open New"},
                 null,
                 {"id": "ZoomIn", "label": "Zoom In"},
                 {"id": "ZoomOut", "label": "Zoom Out"},
                 {"id": "OriginalView", "label": "Original View"},
                 null,
                 {"id": "Quality"},
                 {"id": "Pause"},
                 {"id": "Mute"},
                 null,
                 {"id": "Find", "label": "Find..."},
                 {"id": "FindAgain", "label": "Find Again"},
                 {"id": "Copy"},
                 {"id": "CopyAgain", "label": "Copy Again"},
                 {"id": "CopySVG", "label": "Copy SVG"},
                 {"id": "ViewSVG", "label": "View SVG"},
                 {"id": "ViewSource", "label": "View Source"},
                 {"id": "SaveAs", "label": "Save As"},
                 null,
                 {"id": "Help"},
                 {"id": "About", "label": "About Adobe CVG Viewer..."}
             ]
           }
         },
    "p": [ "$.menu.items[?(@ && @[\'id\'] && !@[\'label\'])].id",
           "$.menu.items[?(@ && @[\'label\'] && strpos(@[\'label\'],\'SVG\')!==false)]",
           "$.menu.items[?(!@)]"
         ]
  },
  { "o": { a: [1,2,3,4],
           b: [5,6,7,8]
         },
    "p": [ "$..[0]",
	       "$..[-1:]",
		   "$..[?(@%2==0)]"
         ]
  },
  { "o": { lin: {color:"red", x:2, y:3},
           cir: {color:"blue", x:5, y:2, r:1 },
           arc: {color:"green", x:2, y:4, r:2, phi0:30, dphi:120 },
           pnt: {x:0, y:7 }
         },
    "p": [ "$.\'?(@[\'color\'])\'.x",
           "$[\'lin\',\'arc\'].color"
         ]
  },
  { "o": { text: [ "hello", "world2.0"] },
    "p": [ "$.text[?(count(@) > 5)]",
           "$.text[?(substr(@,0,1) == \'h\')]"
         ]
  },
  { "o": { a: { a:2, b:3 },
           b: { a:4, b:5 },
           c: { a: { a:6, b:7}, c:8}
         },
    "p": [ "$..a"
         ]
  }
]
';

$parser = new Services_JSON(SERVICES_JSON_LOOSE_TYPE);
$tests = $parser->decode($json);
$out = "";

for ($i=0; $i<count($tests); $i++) {
   for ($j=0; $j<count($tests[$i]['p']); $j++) {
      $pre = ">";
      if (  ($pathes = jsonPath($tests[$i]['o'], $tests[$i]['p'][$j], array('resultType' => "PATH")))
		  &&($values = jsonPath($tests[$i]['o'], $tests[$i]['p'][$j])))
         for ($k=0; $k<count($pathes); $k++) {
            $out .= $pre . " " . $pathes[$k] . " = " . $parser->encode($values[$k]) . "\n";
            $pre = " ";
         }
   }
   $out .= "<hr/>";
}

print($out);
?>
</pre>
</body>
</html>
