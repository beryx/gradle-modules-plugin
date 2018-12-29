package examples.greeter

import examples.greeter.api.Greeter
import org.codehaus.groovy.runtime.IOGroovyMethods

class Friendly implements Greeter {
    @Override
    String hello() {
        var stream = this.getClass().getResourceAsStream('/greeting.txt')
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, 'utf-8'))
        IOGroovyMethods.withCloseable(reader) {
            return reader.readLine()
        }
    }
}
