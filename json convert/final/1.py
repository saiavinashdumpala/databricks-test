import json
from os import read
import copy


# opening raw data file 
def file_reading(file_name):
  f=file_name
  file=open(f,"r")
  a=file.readlines()
 
  return a


def convert_to_json(data):
  count = 0
  for i in data:
    data[count] = i.replace('>','')  #removing '>' from data
    
    count = count+1
  print(len(data))
  print("end")
  list1=[]
  indexes=[]
  for i in range(len(data)):
    if data[i].split(" ")[0] == "MD00" or data[i].split(" ")[0]=="E\n":
      
      indexes.append(i)
  for i in range(0,len(indexes)):
    j=i+1
    if j==len(indexes):
      break
    else:
      print(indexes[i],indexes[j])
      # list1.append(data[indexes[i]]:data[indexes[j]])
      x=slice(indexes[i],indexes[j])
      list1.append(data[x])
  # print(list1)
    # list1.append(data[1]:data[4])
  print(indexes)
    
#############################################################################
  json_dictionary1 = {}
  json_dictionary2 = {}
  json_dictionary3 = {}
  json_dictionary4 = {}		

  test_dict1={}
  test_dict2={}
  test_dict3={}

  component = []
  json_output = []
  #  Assigning the values from raw data to to dictionaries
  for i in list1[0]:
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
      json_dictionary3["time"] = i.split(" ")[2] 
      json_dictionary3["badDimension"] = i.split(" ")[3]
      json_dictionary3["badElectric"] = i.split(" ")[4] 
      json_dictionary3["badPicked"] = i.split(" ")[5] 
      json_dictionary3["badPlaced"] = i.split(" ")[6] 
      json_dictionary3["badOther"] = i.split(" ")[7]

    if i.split(" ")[0] == "MD07":
      json_dictionary4["dispensed"] = i.split(" ")[1]
      json_dictionary4["time"] = i.split(" ")[2]
  
    test_dict1=copy.deepcopy(json_dictionary2)
    test_dict2=copy.deepcopy(json_dictionary3)
    test_dict3=copy.deepcopy(json_dictionary4)

    component.append(test_dict1)
    component.append(test_dict2)
    component.append(test_dict3)

    # component.append(json_dictionary2)
    # component.append(json_dictionary3)
    # component.append(json_dictionary4)

    json_dictionary1["component"] = component
    # print(json_dictionary2)
    print("@@@@@@@@@@@@@@@@")
    print(test_dict1)

    json_output.append(json_dictionary1)  #appending the result to a list
  print(json_dictionary2)


###########################################################################
  return(json_dictionary1)

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