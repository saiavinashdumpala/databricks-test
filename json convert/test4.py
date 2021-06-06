import json
from os import read

# opening raw data file 
def file_reading(filename):
  try:
    raw_file = open(filename)
    read_file = []
    for i in raw_file:
      read_file.append(raw_file.readline())
    return read_file
  except:
    print("File not found. Check the file name and try again")
    exit()

#function to convert raw data to json dictionary format
def convert_to_json(x):
  try:
    data = x
    count = 0
    for i in data:
      data[count] = i.replace('>','')				#removing '>' from data
      count = count+1
  #initilizing dictionaries and lists    
    json_dictionary1 = {}
    json_dictionary2 = {}
    json_dictionary3 = {}
    json_dictionary4 = {}		
    component = []
    json_output = []
    #  Assigning the values from raw data to to dictionaries
    for i in data:
      if i.split(" ")[0] == "MD00":
        json_dictionary1["layoutName"] = i.split(" ")[1]

      if i.split(" ")[0] == "MD01":
        json_dictionary1["machineName"] = i.split(" ")[1]

      if i.split(" ")[0] == "MD02":
        json_dictionary1["assembledLayouts"] = i.split(" ")[1]

      if i.split(" ")[0] == "MD03":
        json_dictionary1["assembledBoards"] = i.split(" ")[1]

      if i.split(" ")[0] == "MD04":
        json_dictionary1["assemblyTime"] = \
          {'totalTime':i.split(" ")[1],\
          'dispenseTime':i.split(" ")[2],\
            'assemblyTime':i.split(" ")[3]}

      if i.split(" ")[0] == "MD05":
        json_dictionary2["componentName"] = i.split(" ")[1]

      if i.split(" ")[0] == "MD06":
        json_dictionary3["placed"] = i.split(" ")[1]
      if i.split(" ")[0] == "MD06":
        json_dictionary3["time"] = i.split(" ")[2]
      if i.split(" ")[0] == "MD06":
        json_dictionary3["badDimension"] = i.split(" ")[3]
      if i.split(" ")[0] == "MD06":
        json_dictionary3["badElectric"] = i.split(" ")[4]
      if i.split(" ")[0] == "MD06":
        json_dictionary3["badPicked"] = i.split(" ")[5]
      if i.split(" ")[0] == "MD06":
        json_dictionary3["badPlaced"] = i.split(" ")[6]
      if i.split(" ")[0] == "MD06":
        json_dictionary3["badOther"] = i.split(" ")[7]

      if i.split(" ")[0] == "MD07":
        json_dictionary4["dispensed"] = i.split(" ")[1]
      if i.split(" ")[0] == "MD07":
        json_dictionary4["time"] = i.split(" ")[2]
      component.append(json_dictionary2)
      component.append(json_dictionary3)
      component.append(json_dictionary4)

      import copy
      component2=[]
      json_output2=[]
      component2=copy.deepcopy(component)

      json_dictionary1["component"] = component2
      print(json_dictionary1)

      json_output.append(json_dictionary1)  #appending the result to a list
      json_output2=copy.deepcopy(json_output)
    return(json_output2)
  except:
    print("An error at converting raw data to json dictionary")
    
# converting the json dictionary to json
def dict_to_json(data):
  try:
    json_object = json.dumps(data, indent=4)
    return json_object
  except:
    print("An error at converting json dictionary to json")

# function to create a json file
def json_file_create(json_object):
  # Writing to json file
  try:
    with open("sample2.json", "w") as outfile:
      outfile.write(str(json_object))
  except:
    print("File Creation Error")


raw_data=file_reading("Machinerawdata-day30.txt")
json_dict=convert_to_json(raw_data)  # passing raw data to convert it to a json dict
json_obj = dict_to_json(json_dict)  # passing json dictionary to convert it to json object
json_file_create(json_obj)  # passing jason object to create a json file