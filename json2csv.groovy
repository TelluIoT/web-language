import groovy.io.FileType
def headers = ',,key,default,en_HLS,en_PRS,nl,nl_HLS,nl_PRS,no,no_HLS,no_PRS,se,se_HLS,se_PRS,ch,ch_HLS,ch_PRS,de,de_HLS,de_PRS'.split(",")

def languages = [:]
(new File('./i18n/')).eachFileRecurse (FileType.FILES) { file ->
    def s = file.path.split(java.util.regex.Pattern.quote(System.getProperty("file.separator")))
    def filename = s[s.length - 1]
    //print("Found file: " + filename)

    if (filename.endsWith(".json")) {
        languages[filename.split('.json')[0] == 'en' ? 'default' : filename.split('.json')[0]] = (new groovy.json.JsonSlurper().parseText(file.getText('UTF-8')));
    }
    
}


new File('./dist/language2.csv').withWriter { out ->
    languages['default'].each{ k, v ->
        out.print(',,')
        out.print(k + ',')
        headers.each{ h ->
            if (h != "" && h != "key") {
                out.print((languages[h][k] ? languages[h][k] : '') + ',')
            }
        }
        out.print('\n')
    }
}