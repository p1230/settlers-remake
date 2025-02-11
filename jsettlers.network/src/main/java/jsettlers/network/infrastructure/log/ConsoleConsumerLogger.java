/*******************************************************************************
 * Copyright (c) 2020
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.network.infrastructure.log;


import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class ConsoleConsumerLogger extends StreamLogger {
	public ConsoleConsumerLogger(String loggerId, Consumer<StringBuffer> output) {
		super(loggerId, new PrintStream(new OutputStream() {

			private StringBuffer lineBuilder = new StringBuffer();

			@Override
			public void write(int i) {
				lineBuilder.append((char)i);
				if(i == '\n') {
					output.accept(lineBuilder);
					System.out.printf("[%s] %s", loggerId, lineBuilder.toString());
					lineBuilder.setLength(0);
				}
			}
		}, true));
	}
}
