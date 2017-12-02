# Token Ring 
Simple realization of Token Ring protocol via concurrent local queues.
 
# Постановка задачи 
 Задача состоит в построении простой модели доисторического сетевого протокола сети под названием TokenRing и исследовании его свойств. 
 1. Система состоит из N пронумерованных от 0 до N-1 узлов (потоков). Узлы упорядочены по порядковому номеру. После состояния N-1 следует узел 0, т.е. узлы формируют кольцо. 
 2. Соседние в кольце потоки могут обмениваться пакетами. Обмен возможен только по часовой стрелке. 
 3. Каждый поток, получив пакет от предыдущего, отдает его следующему. 
 4. Пакеты не могут обгонять друг друга. 
 Исследуем пропускную способность сети (throughput) и характерное время задержки (latency)
  в зависимости от количества узлов N 
  и количества пакетов P (1...N), находящихся в системе одновременно. 
 Рассмортим возможности улучшения исследуемых характеристик и применимость 
 протокола для различных сценариев использования.
 
# Средства измерения
Для измерения всех временных интервалов использовалось стандартное ``` System.nanoTime() ``` 
поскольку предполагалось, что точности порядка мкс( 10<sup>-6</sup> секунд) будет достаточно для измерений. 
Минимальное время имеет погрешность, связанную с точностью измерения времени внутри JVM. 
[nanoTime()-JavaDoc](https://docs.oracle.com/javase/7/docs/api/java/lang/System.html#nanoTime())

# Рассчет latency
В качестве latency  будем использовать время прохода круга для специального сообщения с 
destinationID = 0 <br>(такой id невозможен для потоков, поскольку значение получаемое из getId() возвращает 
[положительное значение](https://docs.oracle.com/javase/6/docs/api/java/lang/Thread.html#getId()))</br>

# Рассчет throughput
Для анализа загруженности сети будем анализировать изменение того же показателя, (скорости обхода круга сообщением)
варьируя при этом количество сообщений в системе. Характерное увеличение времени обхода позволит
нам судить о переносимости системой нагрузок.

# Особенности измерений
В силу ограниченности физических ресурсов будем использовать следующие количества узлов - { 2, 4, 5, 9, 13, 17, 25 },
 а в качестве количества сообщений - { 1, 3, 7, 9, 12, 15, 20, 27, 35, 40 }. Выбор таких множеств входных
данных позволит оценить эффекты появляющиеся в следствии распараллеливания и повышения количества запросов, 
поступающих в систему.

# Анализ результатов измерений
Для реализации очереди использовалась - ConcurrentLinkedQueue. ([JavaDoc](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html))
 
Каждый из узлов содержал свою локальную очередь. 
Передача сообщений осуществлялась следующим образом: "сосед" добавляет в конец очереди сообщение,
сам узел постоянно вычитывает сообщение из головы очереди. Такая организация работы 
является практически хрестоматийной. Разделение "областей", где происходит считывание и запись, 
позволяет предотвратить изменение порядка сообщений при передаче.
 
Локальность очередей позволяет увеличивать количество узлов произвольным образом.
Отсутствие общего звена синхронизации позволяет избежать нелинейного роста времени отклика
за счет усложнения логики взаимодействия потоков и позволяет понизить требования по 
производительности и качеству реализации ядра синхронизации.

Обязанности по синхронизации возложены на каждый узел в отдельности, что позволяет использовать 
любой способ персылки сообщений между узлами - от сетевого до IPC.

Распределенность расходов на синхронизацию говорит о том, что практически любые 
нелинейности могут быть оюъяснены либо топологией сети, либо количеством сообщений 
и необходимости выделения памяти для них.

Также независимость работы каждого узла позволяет организовать их в произвольную
топологию.
## Latency
Результаты работы в абсолютных значениях: 
![png](plots/out_raw.png)
Даже на необработанных данных легко увидеть следующую тенденцию: при увеличении количества
узлов в системе - время на обработку одного сообщения возрастает, за счет увеличения 
критического пути (количество узлов, которое необходимо пройти, для возвращения сообщения в 
исходный пункт), однако время обработки большого числа сообщений уменьшается, поскольку возрастает
нагрузка на 1 узел. То есть при увеличении количества сообщений при большом количестве узлов, сообщения
распределяются по большему числу узлов.

![png](plots/out_1_norm.png)
На отнормированных данных виден линейный рост "задержки" от числа сообщений. Этот 
график - копия предыдущего, однако каждое значение отнормировано на количество узлов 
в системе.

## Throughput
Аналогичные величины в абсолютных значениях:
![png](plots/out_2_raw.png)
По оси ординат снова время прохода сообщением полного круга.
Колебание времени обработки при 1 сообщении в системе - не более чем
эффект связанный с измерением, или влияние операционной системы. Возможно 
планировщик задач не выделил достаточно ресурсов для измеряего процесса. 
Для подтверждения этого предположения привожу пример предыдущих измерений:
![png](plots/output_2.png)

Отнормированные данные более информативны:
![png](plots/out_throu_ns.png)
Этот график является, по сути, зеркальным аналогом отнормированного 
графика роста задержки с увеличением количества сообщений в сисетеме.

![png](plots/out_throu_msg.png)
Этот график иллюстрирует более классическое определение пропускной 
способности - количество сообщений в секунду. 

Заметен следующий паттерн - возрастание величины пропускной способности при достижении равенства 
количества узлов и количества сообщений с последующим спадом. (Особенно характерны 7 сообщений и 3) Что можно обяснить 
оптимальностью потока в системе. Последующая деградация объяняется необходимостью хранения сообщений
на узле, а соответственно и необходимостью выделять и удалять неиспользуемые элементы.

# Возможные оптимизации
Текущая реализация использует очередь размер которой динамически определяется, 
это является первым кандидатом для оптимизаций. Очередь с фиксированной длиной 
должна показывать меньшую деградацию. Однако возникает вопрос что делать с сообщениями, которые 
"не помещаются" в очередь. Также исследуемая модель предполагает одновременное присутствие 
только 2х потоков в очереди, что может быть использовано для упрощения механизма синхронизации
и, таким образом, сокращения расходов на него.

# Инструментарий
"Обвязка" для исследования искомых характеристик (jupyter notebook)

```python
import subprocess
import numpy as np

summary = { "latency": [],
            "cores": [],
            "messages": []}

for cores in [2, 4, 5, 9, 13, 17, 25]:
    for messages in [1, 3, 7, 9, 12, 15, 20, 27, 35, 40]:
        proc = subprocess.Popen(["D:\\Program Files\\Java\\jdk1.8.0_91\\bin\\java", 
                        "-jar", "D:\\java_projects\\token\\target\\token-1.0-SNAPSHOT.jar", 
                        str(cores), str(messages)])
        try:
            proc.wait(20)
        except subprocess.TimeoutExpired:
            proc.terminate()

        log = open('D:\python_proj\log.txt', 'r+')

        nodes = {}
        log = log.readlines()
        for line in log[:-1]:
            node_name = line.split()[1]
            if node_name in nodes:
                nodes[node_name].append(line.split()[5])
            else:
                nodes[node_name] = [line.split()[5]] 

        print("Nodes: %d" % len(nodes.keys()))
        print("Messages: %d" % messages)
        average = []
        for key in nodes: 
            #plt.figure(figsize=(10,8))
            data = nodes[key]
            data = np.array(data).astype(np.long)
            average.append(np.mean(data))
            print("Mean:   %f" % np.mean(data))
            print("Median: %f" % np.median(data))
            #plt.plot(data)
            #plt.show()
        summary["latency"].append(np.mean(average))
        summary["messages"].append(messages)
    summary["cores"].append(cores)
    plt.figure(figsize=(10,8))
    plt.plot(summary["messages"], summary["latency"], "o", color=next(colors))
    plt.xlabel("Messages Cores: %d" % cores)
    plt.ylabel("Latency")
    plt.show()
    
plt.figure(figsize=(10,8))
plt.plot(summary["cores"], summary["latency"], "bo")
plt.show()
```
```python
import subprocess
import matplotlib
import numpy as np
import matplotlib.pyplot as plt
%matplotlib inline
#from threading import Timer

summary = { "latency": [],
            "cores": [],
            "messages": []}

colors = iter(['r', 'c', 'g', 'b', 'm','y'])

for cores in [2, 4, 5, 9, 13, 17, 25]:
    for messages in [1, 3, 7, 9, 12, 15, 20, 27, 35, 40]:
        proc = subprocess.Popen(["D:\\Program Files\\Java\\jdk1.8.0_91\\bin\\java", 
                        "-jar", "D:\\java_projects\\token\\target\\token-1.0-SNAPSHOT.jar", 
                        str(cores), str(messages)])
        try:
            proc.wait(20)
        except subprocess.TimeoutExpired:
            proc.terminate()

        log = open('D:\python_proj\log.txt', 'r+')

        nodes = {}
        log = log.readlines()
        for line in log[:-1]:
            node_name = line.split()[1]
            if node_name in nodes:
                nodes[node_name].append(line.split()[5])
            else:
                nodes[node_name] = [line.split()[5]] 

        print("Nodes: %d" % len(nodes.keys()))
        print("Messages: %d" % messages)
        average = []
        for key in nodes: 
            data = nodes[key]
            data = np.array(data).astype(np.long)
            average.append(np.mean(data))
            print("Mean:   %f" % np.mean(data))
            print("Median: %f" % np.median(data))
        summary["latency"].append(np.mean(average))
        summary["messages"].append(messages)
    summary["cores"].append(cores)
    
colors = iter(cm.rainbow(np.linspace(0, 1, 7)))

# Latency/Messages plot
plt.figure(figsize=(24,20))
start = 0
it = 0 
matplotlib.rcParams.update({'font.size': 18})
for i in range(10, len(summary["messages"]), 10):
    curr_color = next(colors)
    plt.plot(summary["messages"][start:i], summary["latency"][start:i], "o", color=curr_color, label=(str(summary['cores'][it]) + " nodes")) 
    plt.plot(summary["messages"][start:i], summary["latency"][start:i], '--',  color = curr_color )
    start += 10
    it += 1
plt.xticks(np.arange(min(summary["messages"]), max(summary["messages"])+1, 2.0))
plt.yticks(np.arange(min(summary["latency"]), max(summary["latency"])+1, 200.0))
plt.grid()
plt.legend()
plt.xlabel("Number of sent messages", fontsize=24)
plt.ylabel("One message full circle time, ns", fontsize=24)
plt.show()

# Latency/Cores plot
colors = iter(cm.rainbow(np.linspace(0, 1, 11)))

plt.figure(figsize=(24,20))
start = 0
it = 0 
matplotlib.rcParams.update({'font.size': 18})
# print(len(summary['messages'][:-10]))
for i in range( 10 ):
    curr_color = next(colors)
    plt.plot(summary['cores'][:-1], summary["latency"][i::10][:-1], "o", color=curr_color, label=(str(summary['messages'][i::10][0]) + " messages")) 
    plt.plot(summary['cores'][:-1], summary["latency"][i::10][:-1], '--',  color = curr_color )
plt.xticks(np.arange(min(summary["cores"]), max(summary["cores"])+1, 1.0))
plt.yticks(np.arange(min(summary["latency"]), max(summary["latency"])+1, 200.0))
plt.grid()
plt.legend()
plt.xlabel("Number of cores", fontsize=24)
plt.ylabel("One message full circle time, ns", fontsize=24)
plt.show()
```