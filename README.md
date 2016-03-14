# re-complete
re-complete is a text completion library for re-frame applications

Library provides the possibilty to set `new-item-regex` and `sort-fn` through options. Using the `new-item-regex` option,
the user can describe which type of characters he wants to ignore at the beginning and at the end of the word.

For example, if the `new-item-regex` has a value `"()"` , the first eligible item of the input to autocomplete
will be the first item after the `(` ,or `)`.

![alt tag](http://s21.postimg.org/hc3lopv6v/Screen_Shot_2016_03_14_at_15_13_14.png)

If the `new-item-regex` is not set, the first item of the input to autocomplete in this particular case is `(`.
The list of the items to autocomplete is empty because in my autocompletion-data I don't have any words starting on `(`.

![alt tag](http://s14.postimg.org/90jw4k7a9/Screen_Shot_2016_03_14_at_15_13_27.png)

The `sort-fn` option should be the function for sorting items in the autocompletion list.
For example if the value of the `sort-fn` will be `count`, the items to autocomplete will be sorted by the count of their characters.
If the `sort-fn` option is not provided, items to autocomplete are sorted by natural order (alphabetically).

# Usage

The re-autocomplete library has only two functions you need to use.

The function [autocomplete-fn](https://github.com/ScalaConsultants/re-complete/blob/master/src/re_complete/core.cljs#L23)
is the function you should call in you `on-change` handler, it's responsible for updating the state of the autocomplete
in the app-db.

This functions takes as arguments:
* `linked-component-key` - this is the name for the input
* `input` - current value of the input
* `autocomplete-data` - list of the strings to autocomplete
* `options` - optional argument, map of the options (`new-item-regex` , `sort-fn`)

[example of the use](https://github.com/ScalaConsultants/re-complete/blob/master/demo/re_complete/example.cljs#L80)

```clojure
[:input {:type "text"
         :placeholder (str list-name " name")
         :value ""
         :on-change (fn [event]
                      (autocomplete/autocomplete-fn "vegetables"
                                                    (.. event -target -value)
                                                   '("carrot" "asparagus" "broccoli")
                                                    {:options {:new-item-regex "[]()"
                                                               :sort-fn count}}))}]
```

When the change of input occurred (for example we will write `a` to input), in our app-state we will have

```clojure
{:autocomplete {:linked-components                                                                                                                                                                          
                {:vegetable {:text ""
                             :change-index 0
                             :current-word "a"
                             :completions ["asparagus"]
                             :options {:new-item-regex "[]()",
                                       :sort-fn count}}}}}
```

The second function we need is [autocompletion-list](https://github.com/ScalaConsultants/re-complete/blob/master/src/re_complete/core.cljs#L6) 
This function displays list of the items for autocompletition. After click on the item, the item is placed in the right position in text. 

`autocompletion-list` takes as an argument `linked-component-key` - name of the input

[example of the use](https://github.com/ScalaConsultants/re-complete/blob/master/demo/re_complete/example.cljs#L94)

```clojure
[:div.autocompletion-list-part
  [autocomplete/autocompletion-list "vegetable"]]
```

# CSS styling

The `autocompletion-list` function renders `ul` with class `autocompletion-list` and `li` items with class `autocompletion-item`.

Enjoy the library!

# License

Copyright © 2016

Distributed under the Eclipse Public License.

