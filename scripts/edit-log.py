import argparse
import xml.etree.ElementTree as ET

def edit(logfile):
    tree = ET.parse(logfile)
    root = tree.getroot()
    for appender in root.findall('appender'):
        name=appender.get('name')
        appender.set('class', 'ch.qos.logback.core.ConsoleAppender')
        removeItems =[]
        for item in appender:
            if item.tag.lower() != "encoder":
                removeItems.append(item)
            else:
                pattern=item.find('pattern')
                if not pattern is None:
                    value=pattern.text
                    pattern.text = value.replace("%d ", "%d ["+name+"] ")
        
        for elem in removeItems:
            appender.remove(elem)

    tree.write(logfile,'UTF-8',True)
    
parser = argparse.ArgumentParser(description="Modify logback.xml file")
group = parser.add_argument_group('Required')
group.add_argument('--logfile', action="store", dest='logfile', required=True, help='log file')
args = parser.parse_args()

edit(args.logfile)
