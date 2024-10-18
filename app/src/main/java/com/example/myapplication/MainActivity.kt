package com.example.myapplication

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.example.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

class MainActivity : AppCompatActivity() {
    val args = mutableMapOf<String, MyObject>()
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        args["null"] = MyObject("", null)
        args["this"] = MyObject(this.javaClass.name, this)
        args["root"] = MyObject(binding.content.javaClass.name, binding.content)

//        binding.content.addView(args["root"]?.realObject as android.view.View, ConstraintLayout.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        ).apply {
//            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
//        })

        binding.button.setOnClickListener {
            val line = binding.editText.text
            core(listOf(line.toString()))
        }

//        val glide = Glide.with(this)
//        val requestBuilder: RequestBuilder<Drawable> = glide.load("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAPsAAADJCAMAAADSHrQyAAAAclBMVEX///8Amf8Alv8AlP8Ak/8AkP8Aj//7/f/z+f/l8f/O5f+Yyv/p8/+jz//S5/+ezf+v1f+p0v+12P/b7P+LxP+Sx/9js/9csP/3+/9Cp//G4f/a6/98vf9yuf/B3v/K4/8anf8wov+DwP9QrP8sof93u/9viCIGAAALiUlEQVR4nO1cbWOqPAydpVVUfGOKw6nz9f//xQeBlvakILtDd69PzrfVUpo2OU3SsLc3BoPBYDAYDAaDwWAwGAwGg8FgMBgMBoPBYDAYDAaDwWAwGAwGg8FgMBgMxv8XgzAMP357Es/HcH5NpcoRHHfjr4G/2yA5XuOQNH+s09O+5hGKKE2Xmz+eaccIxwcViJ6BEFKdY1/PsRQi6J9x5kt5e2T33u51ich6y230w1l3gWjXt+Q28stgTPuu855CHYdOc1o095NWe1+8TfSvnUz/Jzj5JM8R9KbYOdAr05/bzcoozBCfoAi/1fuBGIk6yW/oz9zekap+mlTNn9I0q/vibKzenx2L8x0s+w2S3ybn6v2+mnavXynFJLAeufvOsbXakvLms7C2ZKnZeYfWlraSHExzYjUHq7svtVl13blMLZE26buWZVD3gDTL4j5w763OS/u/ZPLHFqL3xMl6Qjm/7MrWyGmWhCBdhO4gd9XkIWiz6xlUtfHvrolcyua90xwsml+7cXqL9HEC1mMX9AjEzbWDJbFkid1HZNnssMDdnRy744vHSViLhaN6xTTU9it8Cyfu7oqzeWbrTluVzem3ZF+7g/QfJ2Id3unhpq5lHHPyi5gRBKxV2eyuYjChb7MBaqWaez8CByJ6MNK/gVlX5OUKqU11Ct33jS8OQd8ODxGvCSc0dtGzvAzYSO2+gpBiWzTbno0IZK/Zp9/AIMljBKwH1XhhO1gwvWXZvHAXTJNg6dlkYqvDbjLCdwGQ6jwB02NBjjfXDQfZ9Tm+c5/StrCTN7Evu8W0TRQHVCe/upXsLvboyio3WAe71n4ncIQ++D+213Zi50Cqe7ZDD5Kj0X2A7Gdvs3FtvoUIj1Zvr/d4Mp7FLVMh30KM2y7dJN3Qf8B/1ZiChXC/8ni00Xyb9qQKLsligIN4qC5OpAwCkRmSOHWuFXi+YYIG51fKPnPVFXzXj69ZIpQUfUxFxakKhKHDFF6NzsBglYlcvVrdcRa+C2LtGHkBn2t7T/xUl2G62B0ysfPfgbz2B9kUNkg4FmbYW3m06wdAkifHDPh1WrlxViW9xZnYVqJTOfaTUM/ZO0iB9wPNJ8guA70hTodQ7RlkL6JYZClNdeAqHKyBosO9UNHhy7E3idTvkPKWuO0kdQKLX+rF3k916OxZOjoM7oku7FTt2Z9EsmKpHwPnE2AiHhWj7LDyU12Ns5ch8gTJ+O4q2xvW6ghhzz/GiPg16JZAlK756Fzj1UGzybwO7kqe9Tb6HEG+ODiaE+FeMqQ9TqjyJHGCPVSx7rBmOvZEZ88MQyhVCWIDpncIv8jTW6RfKDq7w8Clp6mGo3eCaAnHojM6e0c9ygrWKjgPiTNv1n3Qgx8Si1s7M3jC8hLv1gbYoxAHLEEHsGBCJrOJkWJw9Q2ie59RId5sEu0qpTdHAlLIJF9I84XKgSVolppAsybOCwxS7N3W33uG9rSx16mzfd/iCkvsAXyuqQbMV7MUOntlLLxARi2ajyBj0TsCHSlMwSx2Z/ZOIvcj9kD9KwkdLEGzVM1Kgnap0tHF09M/J5UfLIZEA+fa8wdA0alnQ9y+/AyEHJ5mqdAf7o79EQH6QUXzxksw76aVWOWfAt9DFOoTtbVQjLmfpTDkW3lXWGeFMEgqHEaghnKfK5XviuowS1ol4zRgflrKaw1LgbkXOdrYu7/UD8qPmI3f2TKtd/K+7UHSJiSI2yHVFVLW7CRQXameR7ezCWvRD8qDKOSXIp1RbcGhI9ExJeORHRMbhZS4OZrTcLTiJTV5KfSD8nayG8U+V+9vse0f0XB4nxSo7ODWEaMopAQq1pyGVFdkoDANrc0K/aCdp3Op8uaMvGPtw3i5PiilpFQqHTdnuKjOn9wOxLPJbRUzfHrFkOqKEhUgL5Pg8Sa9kOlyYQdG4xtIPox3wqTD8kf73tow05/IDslC3IbcJkJ0BjX/INV9+d5hvCdf0stPQMYDk3Ux3GB+7tP0QL/xZoTkkCDTvEbP5ibOhehl5JWmyFeh0248CBzE07mImD/75FkX053y50WCJrUnvSF8J1KGviIF4R/tkDeC22zYFPXh6OlczKf6yytDfMQageplTfRAIiY3r0pU8PAWkl03hoJUV+SrjvACHSjiLeTJ0znXQ6NNXmNf9JoKpGTD/R4mJoDsMH8tTiOPcpWchlRXRj3o+WsBMOkVezrf1s/UPvisdy7ulIY1pDZJDOtm5zGR2fNkjausOlJdnq/CY9TkZoBKcseBcu9ipI3dw9px456XM66Vnfgd7tUIMQkvdA4eqc6nDNVcMA14a3un/oZuUYTip8f7kjcW+Hk6W2zXZvBKHPAB/QkenXkY+kI+kjk1ySsF5axv0frOPYdGfckeMXg7L0LcPi907If3tQVzgCEYBwLz+/n2kEDGCIC7vqzldjK9Wq7/9NRXnfWpSE5bL3QyAcJxfx7fXFbAotcEcSXQ1uPGkmeArHXvjrSzUMl8GoVhiEFczdhlDh7C3TJfhbd5OkHgLTqgOl/86F5ofvoN/XZNXX7rcfPoq+Wprdkj17B57yAfpGWlZTmSv9wOZS91Hg+Y4nghXFfI5FhsmHjmJQIlz6fF5j3M2eoj+txPrsfyVrS+zK1VGa3znhrZU28zHpOF00xDgryZhLW3mR/tq9zBiUoupNjFXkb7nKxvNQC1GT5PWWGz5BFsjg7M0IkpWGOGyn2b5CcZthzEs6O2szVYket7oQ6rxk8ONqeGkH/c7iQrp57Q24pe5NuzMrDd4w5f9pud73jKB6EMIyrHbEj2PNvx08/upNet6Lx4140zycxVsthvaAi2/Rrt5yQ66QX+0gu1Xp4SDN9v7atcgd4nKZFcnX+evDu3FF4luel5jsWMYekYQkp5987d6i78J1em1elFkZECte3kQ4OkldofSsP2bM7TIcWsdQ3fHUxqvwyrYBJt1++eDF1DyEtjOuqbGKZ33WOTGScFeX8mwB8/qNKuK0839emPAlURUheyC0yHtX2u3/Zb029hurM9QQrTcdX6YKgX4ew5y+8/JcXqYRW3X8tU3Sg1Aympt0qmagqBapbEk1sJUu9nKmX/msMhU/YuzdyH6GuyPC3HMdZ7WmFk6LFWoS5zz5oIqa4jaBf93Fnzni6ZZ76Ns6Mcz/JAHcbP+1QaXVfbzAZrhxyEUL1T5lkOzi5nZIKvb1s1sOMPoY6lE3qF00Xk/W9nVzRLS/W7QSq5Xjzoq8H9eDne46Li7TN8xbNJ+zKfWBbyBevZO22W6nja6EN4mhSyZM27Kuc4yguLzTDnlU3g0/lytz6vk+1s/7CvJUdC5mt7mTjewqQm2WQQZku2XM3iUQTNs8xkVos90vFosVqO5xB3DBenJNltl7N4+gvfAZtEaOYlLi3pIfP4S19rPhTO9YOw8ju+26gXA1yzmAJ1zF6TWtN/H1jL1uuXtzhYUfMrH+k+FAO8dDK3vBCwCMyP//vAS7HU5MZAHVr844p/DSB6VXlQ91nE68C9LLOLLvAbgI4/UPoL4CTPhV1PCgUG6vX+r5Vze2hf7UfeMoJXgpNWdhw3/N7/EemC34UTrtjV4zWfwL4S7Ls45/4Dr9Zeb9udO2b7c8yhe031/H9G8ATYfqsdnkMC/ukfpT8D9r5bl9RY2/30/8PwDNhlDlV9FX7O9Xou3Q12PY053U8YvL6eJ5/DErMktCjFNF1nn2D+ZbDr50QaT+Md+fdVr+fRlXAiFhHQG+MXTFlo+OpbHNEPvz3DB4JWRbiiv16SrsJH05WqOL6y6Jl7U6/18tf+ceazcKotYX1Jd87F1rvz8vCCsRvF3Fep+HopaT/CxLkLFlKMX5vkHETjS3ndLZVMuvra9p9BuJmslsuZvyaXwWAwGAwGg8FgMBgMBoPBYDAYDAaDwWAwGAwGg8FgMBgMBoPBYDAYDAaDwfhb8R+juIRXItf8DQAAAABJRU5ErkJggg==")
//        requestBuilder.into(binding.imageView)
        foo()
//        bar()
    }

    private fun core(lines: List<String>) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            Log.d("ttt", line)
            var parsed = line.split(" ").filter { it.isNotEmpty() }
            if (parsed.isEmpty()) {
                i++
                continue
            }
            Log.d("ttt", parsed.toString())
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
                    argsArray.add(MyObject(argParsed[0], fff(argParsed[1]).realObject))
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

    private fun foo() {
        val dd = R.raw.test
        val isr = resources.openRawResource(dd).bufferedReader()

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
        }
        return Class.forName(className)
    }

    private fun fff(args: String): MyObject {
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

    private fun bar() {
//        val className = "android.widget.TextView"
//        val klass = Class.forName(className)
//        val constructor = klass.getConstructor(Class.forName("android.content.Context"))
//        val textView = constructor.newInstance(this)
//        args["textView"] = MyObject(className, textView)
//
//        val method = klass.getMethod("setText", Class.forName("java.lang.CharSequence"))
//        val tt = args["textView"]?.realObject ?: return
//        method.invoke(tt, "hi aaron")
//
//        val method2 = Class.forName("android.view.ViewGroup").getMethod("addView", Class.forName("android.view.View"))
//        method2.invoke(binding.content, tt)
//
//        val constructor2 = Class.forName("android.view.View\$OnClickListener")
//
//        val listener = Proxy.newProxyInstance(
//            constructor2.classLoader,
//            arrayOf(constructor2),
//            InterfaceHandler()
//        )
//        klass.getMethod("setOnClickListener", constructor2).invoke(tt, listener)
//
//        val classs = getClass("android.app.Activity")
//        val method = classs.getMethod("requestPermissions", getClass("[Ljava.lang.String;"), getClass("int"))
//        method.invoke(this, arrayOf("android.permission.READ_EXTERNAL_STORAGE"), 0)
    }

    inner class MyObject(val className: String, val realObject: Any?)

    inner class InterfaceHandler : InvocationHandler {
        override fun invoke(proxy: Any?, method: java.lang.reflect.Method?, args: Array<out Any>?): Any? {
            if (method?.name == "onClick") {
                Log.d("ttt", "hi aaron")
            }
            return null
        }
    }
}
