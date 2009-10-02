var trees = {
  "@encoding": "ISO-8859-1", 
  "@version": "1.0", 
  "nex$nexml": {
    "@version": "0.8", 
    "@xsi:schemaLocation": "http://www.nexml.org/1.0 ../xsd/nexml.xsd", 
    "otus": {
      "@id": "tax1", 
      "@label": "RootTaxaBlock", 
      "otu": [
        {
          "@id": "t1"
        }, 
        {
          "@id": "t2"
        }, 
        {
          "@id": "t3"
        }, 
        {
          "@id": "t4"
        }, 
        {
          "@id": "t5"
        }
      ]
    }, 
    "trees": {
      "@id": "Trees", 
      "@label": "TreesBlockFromXML", 
      "@otus": "tax1", 
      "network": {
        "@id": "tree3", 
        "@label": "tree2", 
        "@xsi:type": "nex:IntNetwork", 
        "edge": [
          {
            "@id": "e1", 
            "@length": "1", 
            "@source": "n1", 
            "@target": "n3"
          }, 
          {
            "@id": "e2", 
            "@length": "2", 
            "@source": "n1", 
            "@target": "n2"
          }, 
          {
            "@id": "e3", 
            "@length": "3", 
            "@source": "n3", 
            "@target": "n4"
          }, 
          {
            "@id": "e4", 
            "@length": "1", 
            "@source": "n3", 
            "@target": "n7"
          }, 
          {
            "@id": "e5", 
            "@length": "2", 
            "@source": "n4", 
            "@target": "n5"
          }, 
          {
            "@id": "e6", 
            "@length": "1", 
            "@source": "n4", 
            "@target": "n6"
          }, 
          {
            "@id": "e7", 
            "@length": "1", 
            "@source": "n7", 
            "@target": "n6"
          }, 
          {
            "@id": "e9", 
            "@length": "1", 
            "@source": "n7", 
            "@target": "n8"
          }, 
          {
            "@id": "e8", 
            "@length": "1", 
            "@source": "n7", 
            "@target": "n9"
          }
        ], 
        "meta": {
          "@content": "L", 
          "@datatype": "xsd:string", 
          "@property": "dc:title", 
          "@xsi:type": "nex:LiteralMeta"
        }, 
        "node": [
          {
            "@id": "n1", 
            "@label": "n1"
          }, 
          {
            "@id": "n2", 
            "@label": "n2", 
            "@otu": "t1"
          }, 
          {
            "@id": "n3", 
            "@label": "n3"
          }, 
          {
            "@id": "n4", 
            "@label": "n4"
          }, 
          {
            "@id": "n5", 
            "@label": "n5", 
            "@otu": "t3"
          }, 
          {
            "@id": "n6", 
            "@label": "n6", 
            "@otu": "t2"
          }, 
          {
            "@id": "n7", 
            "@label": "n7"
          }, 
          {
            "@id": "n8", 
            "@label": "n8", 
            "@otu": "t5"
          }, 
          {
            "@id": "n9", 
            "@label": "n9", 
            "@otu": "t4"
          }
        ]
      }, 
      "tree": [
        {
          "@id": "tree1", 
          "@label": "tree1", 
          "@xsi:type": "nex:FloatTree", 
          "edge": [
            {
              "@id": "e1", 
              "@length": "0.34534", 
              "@source": "n1", 
              "@target": "n3"
            }, 
            {
              "@id": "e2", 
              "@length": "0.4353", 
              "@source": "n1", 
              "@target": "n2"
            }, 
            {
              "@id": "e3", 
              "@length": "0.324", 
              "@source": "n3", 
              "@target": "n4"
            }, 
            {
              "@id": "e4", 
              "@length": "0.3247", 
              "@source": "n3", 
              "@target": "n7"
            }, 
            {
              "@id": "e5", 
              "@length": "0.234", 
              "@source": "n4", 
              "@target": "n5"
            }, 
            {
              "@id": "e6", 
              "@length": "0.3243", 
              "@source": "n4", 
              "@target": "n6"
            }, 
            {
              "@id": "e7", 
              "@length": "0.32443", 
              "@source": "n7", 
              "@target": "n8"
            }, 
            {
              "@id": "e8", 
              "@length": "0.2342", 
              "@source": "n7", 
              "@target": "n9"
            }
          ], 
          "node": [
            {
              "@id": "n1", 
              "@label": "n1", 
              "@root": "true"
            }, 
            {
              "@id": "n2", 
              "@label": "n2", 
              "@otu": "t1"
            }, 
            {
              "@id": "n3", 
              "@label": "n3"
            }, 
            {
              "@id": "n4", 
              "@label": "n4", 
              "dict": {
                "@id": "dict1", 
                "boolean": {
                  "$t": "true", 
                  "@id": "has_tag"
                }
              }
            }, 
            {
              "@id": "n5", 
              "@label": "n5", 
              "@otu": "t3"
            }, 
            {
              "@id": "n6", 
              "@label": "n6", 
              "@otu": "t2"
            }, 
            {
              "@id": "n7", 
              "@label": "n7"
            }, 
            {
              "@id": "n8", 
              "@label": "n8", 
              "@otu": "t5"
            }, 
            {
              "@id": "n9", 
              "@label": "n9", 
              "@otu": "t4"
            }
          ]
        }, 
        {
          "@id": "tree2", 
          "@label": "tree2", 
          "@xsi:type": "nex:IntTree", 
          "edge": [
            {
              "@id": "e1", 
              "@length": "1", 
              "@source": "n1", 
              "@target": "n3"
            }, 
            {
              "@id": "e2", 
              "@length": "2", 
              "@source": "n1", 
              "@target": "n2"
            }, 
            {
              "@id": "e3", 
              "@length": "3", 
              "@source": "n3", 
              "@target": "n4"
            }, 
            {
              "@id": "e4", 
              "@length": "1", 
              "@source": "n3", 
              "@target": "n7"
            }, 
            {
              "@id": "e5", 
              "@length": "2", 
              "@source": "n4", 
              "@target": "n5"
            }, 
            {
              "@id": "e6", 
              "@length": "1", 
              "@source": "n4", 
              "@target": "n6"
            }, 
            {
              "@id": "e7", 
              "@length": "1", 
              "@source": "n7", 
              "@target": "n8"
            }, 
            {
              "@id": "e8", 
              "@length": "1", 
              "@source": "n7", 
              "@target": "n9"
            }
          ], 
          "node": [
            {
              "@id": "n1", 
              "@label": "n1"
            }, 
            {
              "@id": "n2", 
              "@label": "n2", 
              "@otu": "t1"
            }, 
            {
              "@id": "n3", 
              "@label": "n3"
            }, 
            {
              "@id": "n4", 
              "@label": "n4", 
              "dict": {
                "@id": "dict2", 
                "boolean": {
                  "$t": "true", 
                  "@id": "has_tag"
                }
              }
            }, 
            {
              "@id": "n5", 
              "@label": "n5", 
              "@otu": "t3"
            }, 
            {
              "@id": "n6", 
              "@label": "n6", 
              "@otu": "t2"
            }, 
            {
              "@id": "n7", 
              "@label": "n7"
            }, 
            {
              "@id": "n8", 
              "@label": "n8", 
              "@otu": "t5"
            }, 
            {
              "@id": "n9", 
              "@label": "n9", 
              "@otu": "t4"
            }
          ]
        }
      ]
    }
  }
};