private static def parseCSV(rawCSV, headers) {
    def outputList = [];
    
    String str;
    String str2;
    
    BufferedReader reader = new BufferedReader(new StringReader(rawCSV));
    Boolean resume = false;
    def lineData;
    
    try {
        while ((str = reader.readLine()) != null) {
            
            if (resume) {
                lineData = parseCSVLine(str, lineData.list, true)
            } else {
                lineData = parseCSVLine(str, [], false)
            }
            
            
            if (lineData.resume == false) {
                resume = false;
                def outObject = [:]
                for (int i = 0; i < headers.size(); i++) {
                    if (i < lineData.list.size()) {
                        outObject."${ headers[i]}" = lineData.list[i].trim()
                    } else {
                        outObject."${ headers[i]}" = null
                    }
                }
                outputList.add(outObject)
            } else {
                resume = true;
            }
        }
        
    } catch(IOException e) {
        e.printStackTrace();
    }
    
    return outputList
}


private static def parseCSVLine(String line, tokenList, Boolean resume) {
    int safetyCounter = 0;
    
    
    ////System.out.println("TokenList: " + tokenList);
    ////System.out.println("Line: " + line);
    if (resume) {
        def resumeToken = tokenList[tokenList.size() - 1]
        if (resumeToken.endsWith("\"")) {
            resumeToken = resumeToken.subSequence(0, resumeToken.length() - 1)
        }
        line = resumeToken + "\n" + line
        tokenList.pop()
    }
    
    resume = false
    
    while (safetyCounter < 100000 && line.length() > 0) {
        safetyCounter++;
        
        String token = parseCSVToken(line)
        ////System.out.println("Token: " + token);
        if (line.length() < token.length()) {
            ////System.out.println("Split detected. " + token);
            line = "";
            resume = true
        } else {
            line = line.substring(token.length())
            if (line.startsWith(",")) {
                line = line.substring(1);
            }
            
            if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.subSequence(1, token.length() - 1)
            }
            
            token = token.replace("\"\"", "\"");
            
        }
        tokenList.add(token)
        
    }
    
    if (safetyCounter >= 100000) {
        throw new Exception("infinite loop while parsing CSV line")
    }
    
    return ['list':tokenList, 'resume': resume]
}

private static String parseCSVToken(String token) {
    if (token.equals("\"\"")) {
        return "\"\""
    } else if (token.startsWith("\"")) {
        String s = token.substring(1)
        s = s.replace("\"\"", "§§");
        
        String[] sSplit = s.split("\"");
        s = sSplit[0].replace("§§", "\"\"");
        s = "\"" + s + "\"";
        
        return s;
    } else {
        String[] tokenSplit = token.split(",");
        ////System.out.println(tokenSplit);
        if (tokenSplit.length > 0) {
            return tokenSplit[0];
        } else {
            return ""
        }
    }
    
}

def headers = ',,key,default,en_HLS,en_PRS,nl,nl_HLS,nl_PRS,no,no_HLS,no_PRS,se,se_HLS,se_PRS,ch,ch_HLS,ch_PRS,de,de_HLS,de_PRS'.split(",")
def csv = parseCSV((new File("dist/language.csv")).getText('UTF8'), headers);

File fileDir = new File("dist/language.csv");

BufferedReader inx = new BufferedReader(
    new InputStreamReader(
                new FileInputStream(fileDir), "UTF8"));

String str;

while ((str = inx.readLine()) != null) {
    System.out.println(str);
}

inx.close();
		
def csv2 = parseCSV(str, headers);

headers.each{ header ->
    if (header == "ch" && header.equals("key") == false && header.equals("z") == false && header.equals("changed") == false && header != "") {
        def code = header.equals("default") ? "en" : header
        
        //System.out.println("Writing to: " + "messages" + code + ".properties")
    
        def toJson = [:]
        csv.each{ d ->
            def data = d."${header}"
            
            if (d.key != null && d.key != "" && data != null && data != "") {
                toJson[d.key] = data
                System.out.println(":" + header + " " + d.key + " " + data)
            }
        }
        (new File('./i18n/' + code + ".json")).setText(groovy.json.JsonOutput.toJson(toJson), 'UTF-8')
    }
}
