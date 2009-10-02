var characters = {
  "@encoding": "ISO-8859-1", 
  "@version": "1.0", 
  "nex$nexml": {
    "@version": "0.8", 
    "@xsi:schemaLocation": "http://www.nexml.org/1.0 ../xsd/nexml.xsd", 
    "characters": [
      {
        "@id": "m1", 
        "@otus": "taxa1", 
        "@xsi:type": "nex:RestrictionSeqs", 
        "format": {
          "char": [
            {
              "@id": "c1", 
              "@states": "states3"
            }, 
            {
              "@id": "c2", 
              "@states": "states3"
            }, 
            {
              "@id": "c3", 
              "@states": "states3"
            }, 
            {
              "@id": "c4", 
              "@states": "states3"
            }
          ], 
          "states": {
            "@id": "states3", 
            "state": [
              {
                "@id": "s1", 
                "@symbol": "0"
              }, 
              {
                "@id": "s2", 
                "@symbol": "1"
              }
            ]
          }
        }, 
        "matrix": {
          "row": [
            {
              "@id": "r1", 
              "@otu": "t1", 
              "seq": {
                "$t": "0101"
              }
            }, 
            {
              "@id": "r2", 
              "@otu": "t2", 
              "seq": {
                "$t": "0101"
              }
            }, 
            {
              "@id": "r3", 
              "@otu": "t3", 
              "seq": {
                "$t": "0101"
              }
            }, 
            {
              "@id": "r4", 
              "@otu": "t4", 
              "seq": {
                "$t": "0101"
              }
            }, 
            {
              "@id": "r5", 
              "@otu": "t5", 
              "seq": {
                "$t": "0101"
              }
            }
          ]
        }
      }, 
      {
        "@id": "m2", 
        "@label": "Categorical characters", 
        "@otus": "taxa1", 
        "@xsi:type": "nex:StandardCells", 
        "format": {
          "char": [
            {
              "@id": "c1", 
              "@states": "states1"
            }, 
            {
              "@id": "c2", 
              "@states": "states1"
            }
          ], 
          "states": {
            "@id": "states1", 
            "polymorphic_state_set": {
              "@id": "s4", 
              "@symbol": "4", 
              "member": [
                {
                  "@state": "s1"
                }, 
                {
                  "@state": "s2"
                }
              ]
            }, 
            "state": [
              {
                "@id": "s1", 
                "@symbol": "1"
              }, 
              {
                "@id": "s2", 
                "@symbol": "2"
              }, 
              {
                "@id": "s3", 
                "@symbol": "3"
              }
            ], 
            "uncertain_state_set": {
              "@id": "s5", 
              "@symbol": "5", 
              "member": [
                {
                  "@state": "s3"
                }, 
                {
                  "@state": "s1"
                }
              ]
            }
          }
        }, 
        "matrix": {
          "row": [
            {
              "@id": "r1", 
              "@otu": "t1", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "s1"
                }, 
                {
                  "@char": "c2", 
                  "@state": "s2"
                }
              ]
            }, 
            {
              "@id": "r2", 
              "@otu": "t2", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "s2"
                }, 
                {
                  "@char": "c2", 
                  "@state": "s2"
                }
              ]
            }, 
            {
              "@id": "r3", 
              "@otu": "t3", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "s3"
                }, 
                {
                  "@char": "c2", 
                  "@state": "s4"
                }
              ]
            }, 
            {
              "@id": "r4", 
              "@otu": "t4", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "s2"
                }, 
                {
                  "@char": "c2", 
                  "@state": "s3"
                }
              ]
            }, 
            {
              "@id": "r5", 
              "@otu": "t5", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "s4"
                }, 
                {
                  "@char": "c2", 
                  "@state": "s1"
                }
              ]
            }
          ]
        }
      }, 
      {
        "@id": "m3", 
        "@label": "Continuous characters", 
        "@otus": "taxa1", 
        "@xsi:type": "nex:ContinuousCells", 
        "format": {
          "char": [
            {
              "@class": "something", 
              "@id": "c1", 
              "@label": "this is character 1"
            }, 
            {
              "@id": "c2"
            }, 
            {
              "@id": "c3"
            }, 
            {
              "@id": "c4"
            }, 
            {
              "@id": "c5"
            }
          ]
        }, 
        "matrix": {
          "row": [
            {
              "@id": "r1", 
              "@otu": "t1", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "-1.545414144070023"
                }, 
                {
                  "@char": "c2", 
                  "@state": "-2.3905621575431044"
                }, 
                {
                  "@char": "c3", 
                  "@state": "-2.9610221833467265"
                }, 
                {
                  "@char": "c4", 
                  "@state": "0.7868662069161243"
                }, 
                {
                  "@char": "c5", 
                  "@state": "0.22968509237534918"
                }
              ]
            }, 
            {
              "@id": "r2", 
              "@otu": "t2", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "-1.6259836379710066"
                }, 
                {
                  "@char": "c2", 
                  "@state": "3.649352410850134"
                }, 
                {
                  "@char": "c3", 
                  "@state": "1.778885099660406"
                }, 
                {
                  "@char": "c4", 
                  "@state": "-1.2580877968480846"
                }, 
                {
                  "@char": "c5", 
                  "@state": "0.22335354995610862"
                }
              ]
            }, 
            {
              "@id": "r3", 
              "@otu": "t3", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "-1.5798979984134964"
                }, 
                {
                  "@char": "c2", 
                  "@state": "2.9548251411133157"
                }, 
                {
                  "@char": "c3", 
                  "@state": "1.522005675256233"
                }, 
                {
                  "@char": "c4", 
                  "@state": "-0.8642016921755289"
                }, 
                {
                  "@char": "c5", 
                  "@state": "-0.938129801832388"
                }
              ]
            }, 
            {
              "@id": "r4", 
              "@otu": "t4", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "2.7436692306788086"
                }, 
                {
                  "@char": "c2", 
                  "@state": "-0.7151148143399818"
                }, 
                {
                  "@char": "c3", 
                  "@state": "4.592207937774776"
                }, 
                {
                  "@char": "c4", 
                  "@state": "-0.6898841440534845"
                }, 
                {
                  "@char": "c5", 
                  "@state": "0.5769509574453064"
                }
              ]
            }, 
            {
              "@id": "r5", 
              "@otu": "t5", 
              "cell": [
                {
                  "@char": "c1", 
                  "@state": "3.1060827493657683"
                }, 
                {
                  "@char": "c2", 
                  "@state": "-1.0453787389160105"
                }, 
                {
                  "@char": "c3", 
                  "@state": "2.67416332763427"
                }, 
                {
                  "@char": "c4", 
                  "@state": "-1.4045634106692808"
                }, 
                {
                  "@char": "c5", 
                  "@state": "0.019890469925520196"
                }
              ]
            }
          ]
        }
      }, 
      {
        "@id": "c3", 
        "@label": "DNA sequences", 
        "@otus": "taxa1", 
        "@xsi:type": "nex:DnaSeqs", 
        "format": {
          "char": [
            {
              "@id": "c1", 
              "@states": "states7"
            }, 
            {
              "@id": "c2", 
              "@states": "states7"
            }, 
            {
              "@id": "c3", 
              "@states": "states7"
            }, 
            {
              "@id": "c4", 
              "@states": "states7"
            }, 
            {
              "@id": "c5", 
              "@states": "states7"
            }, 
            {
              "@id": "c6", 
              "@states": "states7"
            }, 
            {
              "@id": "c7", 
              "@states": "states7"
            }, 
            {
              "@id": "c8", 
              "@states": "states7"
            }, 
            {
              "@id": "c9", 
              "@states": "states7"
            }, 
            {
              "@id": "c10", 
              "@states": "states7"
            }, 
            {
              "@id": "c11", 
              "@states": "states7"
            }, 
            {
              "@id": "c12", 
              "@states": "states7"
            }, 
            {
              "@id": "c13", 
              "@states": "states7"
            }, 
            {
              "@id": "c14", 
              "@states": "states7"
            }, 
            {
              "@id": "c15", 
              "@states": "states7"
            }, 
            {
              "@id": "c16", 
              "@states": "states7"
            }
          ], 
          "states": {
            "@id": "states7", 
            "state": [
              {
                "@id": "s1", 
                "@symbol": "A"
              }, 
              {
                "@id": "s2", 
                "@symbol": "C"
              }, 
              {
                "@id": "s3", 
                "@symbol": "G"
              }, 
              {
                "@id": "s4", 
                "@symbol": "T"
              }
            ], 
            "uncertain_state_set": [
              {
                "@id": "s5", 
                "@symbol": "K", 
                "member": [
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s6", 
                "@symbol": "M", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s2"
                  }
                ]
              }, 
              {
                "@id": "s7", 
                "@symbol": "R", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s3"
                  }
                ]
              }, 
              {
                "@id": "s8", 
                "@symbol": "S", 
                "member": [
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s3"
                  }
                ]
              }, 
              {
                "@id": "s9", 
                "@symbol": "W", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s10", 
                "@symbol": "Y", 
                "member": [
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s11", 
                "@symbol": "B", 
                "member": [
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s12", 
                "@symbol": "D", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s13", 
                "@symbol": "H", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s14", 
                "@symbol": "V", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s3"
                  }
                ]
              }, 
              {
                "@id": "s15", 
                "@symbol": "N", 
                "member": [
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s4"
                  }, 
                  {
                    "@state": "s2"
                  }
                ]
              }, 
              {
                "@id": "s16", 
                "@symbol": "X", 
                "member": [
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s4"
                  }, 
                  {
                    "@state": "s2"
                  }
                ]
              }, 
              {
                "@id": "s17", 
                "@symbol": "-"
              }, 
              {
                "@id": "s18", 
                "@symbol": "?", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s4"
                  }, 
                  {
                    "@state": "s5"
                  }, 
                  {
                    "@state": "s6"
                  }, 
                  {
                    "@state": "s7"
                  }, 
                  {
                    "@state": "s8"
                  }, 
                  {
                    "@state": "s9"
                  }, 
                  {
                    "@state": "s10"
                  }, 
                  {
                    "@state": "s11"
                  }, 
                  {
                    "@state": "s12"
                  }, 
                  {
                    "@state": "s13"
                  }, 
                  {
                    "@state": "s14"
                  }, 
                  {
                    "@state": "s15"
                  }, 
                  {
                    "@state": "s16"
                  }, 
                  {
                    "@state": "s17"
                  }
                ]
              }
            ]
          }
        }, 
        "matrix": {
          "row": [
            {
              "@id": "r1", 
              "@otu": "t1", 
              "seq": {
                "$t": "A C G C T C G C A T C G C A T C"
              }
            }, 
            {
              "@id": "r2", 
              "@otu": "t2", 
              "seq": {
                "$t": "A C G C T C G C A T C G C A T C"
              }
            }, 
            {
              "@id": "r3", 
              "@otu": "t3", 
              "seq": {
                "$t": "A C G C T C G C A T C G C A T C"
              }
            }
          ]
        }
      }, 
      {
        "@id": "rnaseqs4", 
        "@label": "RNA sequences", 
        "@otus": "taxa1", 
        "@xsi:type": "nex:RnaSeqs", 
        "format": {
          "char": [
            {
              "@id": "c1", 
              "@states": "states3"
            }, 
            {
              "@id": "c2", 
              "@states": "states3"
            }, 
            {
              "@id": "c3", 
              "@states": "states3"
            }, 
            {
              "@id": "c4", 
              "@states": "states3"
            }, 
            {
              "@id": "c5", 
              "@states": "states3"
            }, 
            {
              "@id": "c6", 
              "@states": "states3"
            }, 
            {
              "@id": "c7", 
              "@states": "states3"
            }, 
            {
              "@id": "c8", 
              "@states": "states3"
            }, 
            {
              "@id": "c9", 
              "@states": "states3"
            }, 
            {
              "@id": "c10", 
              "@states": "states3"
            }, 
            {
              "@id": "c11", 
              "@states": "states3"
            }, 
            {
              "@id": "c12", 
              "@states": "states3"
            }, 
            {
              "@id": "c13", 
              "@states": "states3"
            }, 
            {
              "@id": "c14", 
              "@states": "states3"
            }, 
            {
              "@id": "c15", 
              "@states": "states3"
            }, 
            {
              "@id": "c16", 
              "@states": "states3"
            }, 
            {
              "@id": "c17", 
              "@states": "states3"
            }, 
            {
              "@id": "c18", 
              "@states": "states3"
            }, 
            {
              "@id": "c19", 
              "@states": "states3"
            }, 
            {
              "@id": "c20", 
              "@states": "states3"
            }
          ], 
          "states": {
            "@id": "states3", 
            "state": [
              {
                "@id": "s1", 
                "@symbol": "A"
              }, 
              {
                "@id": "s2", 
                "@symbol": "C"
              }, 
              {
                "@id": "s3", 
                "@symbol": "G"
              }, 
              {
                "@id": "s4", 
                "@symbol": "U"
              }
            ], 
            "uncertain_state_set": [
              {
                "@id": "s5", 
                "@symbol": "K", 
                "member": [
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s6", 
                "@symbol": "M", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s2"
                  }
                ]
              }, 
              {
                "@id": "s7", 
                "@symbol": "R", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s3"
                  }
                ]
              }, 
              {
                "@id": "s8", 
                "@symbol": "S", 
                "member": [
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s3"
                  }
                ]
              }, 
              {
                "@id": "s9", 
                "@symbol": "W", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s10", 
                "@symbol": "Y", 
                "member": [
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s11", 
                "@symbol": "B", 
                "member": [
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s12", 
                "@symbol": "D", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s13", 
                "@symbol": "H", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s4"
                  }
                ]
              }, 
              {
                "@id": "s14", 
                "@symbol": "V", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s3"
                  }
                ]
              }, 
              {
                "@id": "s15", 
                "@symbol": "N", 
                "member": [
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s4"
                  }, 
                  {
                    "@state": "s2"
                  }
                ]
              }, 
              {
                "@id": "s16", 
                "@symbol": "X", 
                "member": [
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s4"
                  }, 
                  {
                    "@state": "s2"
                  }
                ]
              }, 
              {
                "@id": "s17", 
                "@symbol": "-"
              }, 
              {
                "@id": "s18", 
                "@symbol": "?", 
                "member": [
                  {
                    "@state": "s1"
                  }, 
                  {
                    "@state": "s2"
                  }, 
                  {
                    "@state": "s3"
                  }, 
                  {
                    "@state": "s4"
                  }, 
                  {
                    "@state": "s5"
                  }, 
                  {
                    "@state": "s6"
                  }, 
                  {
                    "@state": "s7"
                  }, 
                  {
                    "@state": "s8"
                  }, 
                  {
                    "@state": "s9"
                  }, 
                  {
                    "@state": "s10"
                  }, 
                  {
                    "@state": "s11"
                  }, 
                  {
                    "@state": "s12"
                  }, 
                  {
                    "@state": "s13"
                  }, 
                  {
                    "@state": "s14"
                  }, 
                  {
                    "@state": "s15"
                  }, 
                  {
                    "@state": "s16"
                  }, 
                  {
                    "@state": "s17"
                  }
                ]
              }
            ]
          }
        }, 
        "matrix": {
          "row": [
            {
              "@id": "r1", 
              "@otu": "t1", 
              "seq": {
                "$t": "ACGCUCGCAUCGCAUC"
              }
            }, 
            {
              "@id": "r2", 
              "@otu": "t2", 
              "seq": {
                "$t": "ACGCUCGCAUCGCAUC"
              }
            }, 
            {
              "@id": "r3", 
              "@otu": "t3", 
              "seq": {
                "$t": "ACGCUCGCAUCGCAUC"
              }
            }
          ]
        }
      }, 
      {
        "@id": "c5", 
        "@label": "Continuous sequences", 
        "@otus": "taxa1", 
        "@xsi:type": "nex:ContinuousSeqs", 
        "format": {
          "char": [
            {
              "@id": "c1"
            }, 
            {
              "@id": "c2"
            }, 
            {
              "@id": "c3"
            }, 
            {
              "@id": "c4"
            }, 
            {
              "@id": "c5"
            }
          ]
        }, 
        "matrix": {
          "row": [
            {
              "@id": "r1", 
              "@otu": "t1", 
              "seq": {
                "$t": "-1.545414144070023 -2.3905621575431044 -2.9610221833467265 0.7868662069161243 0.22968509237534918"
              }
            }, 
            {
              "@id": "r2", 
              "@otu": "t2", 
              "seq": {
                "$t": "-1.6259836379710066 3.649352410850134 1.778885099660406 -1.2580877968480846 0.22335354995610862"
              }
            }, 
            {
              "@id": "r3", 
              "@otu": "t3", 
              "seq": {
                "$t": "-1.5798979984134964 2.9548251411133157 1.522005675256233 -0.8642016921755289 -0.938129801832388"
              }
            }, 
            {
              "@id": "r4", 
              "@otu": "t4", 
              "seq": {
                "$t": "2.7436692306788086 -0.7151148143399818 4.592207937774776 -0.6898841440534845 0.5769509574453064"
              }
            }, 
            {
              "@id": "r5", 
              "@otu": "t5", 
              "seq": {
                "$t": "3.1060827493657683 -1.0453787389160105 2.67416332763427 -1.4045634106692808 0.019890469925520196"
              }
            }
          ]
        }
      }, 
      {
        "@id": "c6", 
        "@label": "Standard sequences", 
        "@otus": "taxa1", 
        "@xsi:type": "nex:StandardSeqs", 
        "format": {
          "char": [
            {
              "@id": "c1", 
              "@states": "states1"
            }, 
            {
              "@id": "c2", 
              "@states": "states1"
            }
          ], 
          "states": {
            "@id": "states1", 
            "polymorphic_state_set": {
              "@id": "s4", 
              "@symbol": "4", 
              "member": [
                {
                  "@state": "s1"
                }, 
                {
                  "@state": "s2"
                }
              ]
            }, 
            "state": [
              {
                "@id": "s1", 
                "@symbol": "1"
              }, 
              {
                "@id": "s2", 
                "@symbol": "2"
              }, 
              {
                "@id": "s3", 
                "@symbol": "3"
              }
            ], 
            "uncertain_state_set": {
              "@id": "s5", 
              "@symbol": "5", 
              "member": [
                {
                  "@state": "s3"
                }, 
                {
                  "@state": "s1"
                }
              ]
            }
          }
        }, 
        "matrix": {
          "row": [
            {
              "@id": "r1", 
              "@otu": "t1", 
              "seq": {
                "$t": "1 2"
              }
            }, 
            {
              "@id": "r2", 
              "@otu": "t2", 
              "seq": {
                "$t": "2 2"
              }
            }, 
            {
              "@id": "r3", 
              "@otu": "t3", 
              "seq": {
                "$t": "3 4"
              }
            }, 
            {
              "@id": "r4", 
              "@otu": "t4", 
              "seq": {
                "$t": "2 3"
              }
            }, 
            {
              "@id": "r5", 
              "@otu": "t5", 
              "seq": {
                "$t": "4 1"
              }
            }
          ]
        }
      }
    ], 
    "dict": {
      "@id": "dict1", 
      "string": [
        {
          "$t": "$Author: rvos $", 
          "@id": "author"
        }, 
        {
          "$t": "$Date: 2009-03-13 01:57:49 -0400 (Fri, 13 Mar 2009) $", 
          "@id": "date"
        }, 
        {
          "$t": "$Header: $", 
          "@id": "header"
        }, 
        {
          "$t": "$Id: characters.xml 970 2009-03-13 05:57:49Z rvos $", 
          "@id": "id"
        }, 
        {
          "$t": "$Rev: 970 $", 
          "@id": "rev"
        }, 
        {
          "$t": "$URL: https://nexml.svn.sourceforge.net/svnroot/nexml/trunk/nexml/examples/characters.xml $", 
          "@id": "url"
        }
      ]
    }, 
    "otus": {
      "@id": "taxa1", 
      "@label": "Primary taxa block", 
      "otu": [
        {
          "@id": "t1", 
          "@label": "Homo sapiens"
        }, 
        {
          "@id": "t2", 
          "@label": "Pan paniscus"
        }, 
        {
          "@id": "t3", 
          "@label": "Pan troglodytes"
        }, 
        {
          "@id": "t4", 
          "@label": "Gorilla gorilla"
        }, 
        {
          "@id": "t5", 
          "@label": "Pongo pygmaeus"
        }
      ]
    }
  }
};