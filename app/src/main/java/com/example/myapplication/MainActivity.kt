package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Proxy

class MainActivity : AppCompatActivity() {
    // NOTE: args can cause memory leak or OOM. Because it stores objects that might be not used anymore.
    private val args = mutableMapOf<String, MyObject>()
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        args["null"] = MyObject("", null)
        args["this"] = MyObject(this.javaClass.name, this)
        args["root"] = MyObject(binding.rootOfInstructions.javaClass.name, binding.rootOfInstructions)

        binding.button.setOnClickListener {
            val lines = binding.editText.text.toString().split("\n")
            core(lines)
        }
        readFromFileAndRunCore()
    }

    private fun core(lines: List<String>) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            Log.d(TAG, line)
            var parsed = line.split(" ").filter { it.isNotEmpty() }
            if (parsed.isEmpty()) {
                i++
                continue
            }
            Log.d(TAG, parsed.toString())
            var returned: Any? = null
            if (parsed[0] == "invoke") {
                val className = parsed[1]
                val methodName = parsed[2]
                val argsArray = mutableListOf<MyObject>()
                var invokerObject: Any? = null
                for (i in 3 until parsed.size) {
                    val arg = parsed[i]
                    if (arg == "with") {
                        invokerObject = getArg(parsed[i + 1]).realObject
                        break
                    }
                    val argParsed = arg.split("::")
                    argsArray.add(MyObject(argParsed[0], getArgOrArray(argParsed[1]).realObject))
                }
                returned = invoke(className, methodName, invokerObject, *argsArray.toTypedArray())
            } else if (parsed[0] == "construct") {
                val className = parsed[1]
                val argsArray = mutableListOf<MyObject>()
                for (i in 2 until parsed.size) {
                    val arg = parsed[i]
                    if (arg == "assign") {
                        break
                    }
                    val argParsed = arg.split("::")
                    argsArray.add(MyObject(argParsed[0], getArg(argParsed[1]).realObject))
                }
                returned = construct(className, *argsArray.toTypedArray())
            } else if (parsed[0] == "implement") {
                val interfaceName = parsed[1]
                val methodName = parsed[2]
                val interfaceLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].startsWith("assign")) {
                    interfaceLines.add(lines[i])
                    i++
                }
                parsed = lines[i].split(" ").filter { it.isNotEmpty() }
                val listener = Proxy.newProxyInstance(
                    Class.forName(interfaceName).classLoader,
                    arrayOf(Class.forName(interfaceName))
                ) { proxy, method, args ->
                    if (method.name == methodName) {
                        core(interfaceLines)
                    }
                }
                returned = listener
            } else if (parsed[0] == "set") {
                val className = parsed[1]
                val fieldName = parsed[2]
                val value = getArg(parsed[3].split("::")[1])
                if (parsed[4] != "with") {
                    throw Exception("Invalid set statement")
                }
                val invokerObject = getArg(parsed[5])
                set(className, fieldName, value, invokerObject)
            }
            if (parsed.size >= 2 && parsed[parsed.size - 2] == "assign") {
                args[parsed[parsed.size - 1]] = MyObject("", returned)
            }
            i++
        }
    }

    private fun readFromFileAndRunCore() {
        val isr = resources.openRawResource(R.raw.test).bufferedReader()

        lifecycleScope.launch {
            val lines = mutableListOf<String>()
            var line: String? = withContext(Dispatchers.IO) {
                isr.readLine()
            }
            while (line != null) {
                lines.add(line)
                line = withContext(Dispatchers.IO) {
                    isr.readLine()
                }
            }

            withContext(Dispatchers.IO) {
                isr.close()
            }

            core(lines)
        }
    }

    private fun getClass(className: String): Class<*> {
        if (className == "int") {
            return Int::class.java
        } else if (className == "float") {
            return Float::class.java
        } else if (className == "long") {
            return Long::class.java
        } else if (className == "boolean") {
            return Boolean::class.java
        }
        return Class.forName(className)
    }

    private fun getArgOrArray(args: String): MyObject {
        if (args[0] == '{' && args[args.length - 1]== '}') {
            val argList = args.substring(1, args.length - 1).split(',')
            val ll = mutableListOf<String>()
            for (arg in argList) {
                ll.add(getStringOrNull(arg) ?: continue)
            }
            return MyObject("[Ljava.lang.String;", ll.toTypedArray())
        }
        return getArg(args)
    }

    private fun getStringOrNull(arg: String): String? {
        if (arg[0] == '"' && arg[arg.length - 1] == '"') {
            return arg.substring(1, arg.length - 1)
        }
        return null
    }

    private fun getArg(arg: String): MyObject {
        if (arg[0] == '"' && arg[arg.length - 1] == '"') {
            return MyObject("java.lang.String", arg.substring(1, arg.length - 1))
        }
        val possibleInt = arg.toIntOrNull()
        if (possibleInt != null) {
            return MyObject("int", possibleInt)
        }
        val possibleFloat = arg.toFloatOrNull()
        if (possibleFloat != null) {
            return MyObject("float", possibleFloat)
        }
        val possibleLong = arg.toLongOrNull()
        if (possibleLong != null) {
            return MyObject("long", possibleLong)
        }
        val possibleBoolean = arg.toBooleanStrictOrNull()
        if (possibleBoolean != null) {
            return MyObject("boolean", possibleBoolean)
        }
        return args[arg] ?: MyObject("", null)
    }

    private fun invoke(className: String, methodName: String, invokerObject: Any?, vararg args: MyObject): Any? {
        return try {
            val classs = getClass(className)
            val method = classs.getMethod(methodName, *args.map { getClass(it.className) }.toTypedArray())
            method.invoke(invokerObject, *args.map { it.realObject }.toTypedArray())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun construct(className: String, vararg args: MyObject): Any? {
        return try {
            val classs = getClass(className)
            val constructor = classs.getConstructor(*args.map { getClass(it.className) }.toTypedArray())
            constructor.newInstance(*args.map { it.realObject }.toTypedArray())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun set(className: String, fieldName: String, value: MyObject, invokerObject: MyObject) {
        val classs = getClass(className)
        val field = classs.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(invokerObject.realObject, value.realObject)
    }

    inner class MyObject(val className: String, val realObject: Any?)

    companion object {
        private const val TAG = "MainActivity"
    }
}
