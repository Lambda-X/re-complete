# re-complete
re-complete is a text completion library for re-frame applications

[![via Clojars](http://clojars.org/re-complete/latest-version.svg)](http://clojars.org/replumb)

This library provides the possibilty to set `trim-chars` and `case-sensitive?` through options. Using the `trim-chars` option,
the user can describe which type of characters he wants to ignore at the beginning and at the end of the word.

For example, if the `trim-chars` has a value `"()"`, the first eligible item of the input to autocomplete
will be the first item after the `(` or `)`.

![alt tag](http://s21.postimg.org/hc3lopv6v/Screen_Shot_2016_03_14_at_15_13_14.png)

If the `trim-chars` is not set, the first item of the input to autocomplete in this particular case is `(`.
The list of the items to autocomplete is empty because in my autocompletion-data I don't have any words starting on `(`.

![alt tag](http://s14.postimg.org/90jw4k7a9/Screen_Shot_2016_03_14_at_15_13_27.png)

The `case-sentitive?` option has a default value `false`. So if you want your autocomplete to be case
sensitive, you should set `case-sensitive? true`

# Usage

The re-autocomplete library has only few functions:

For setting the `options` you need to `dispatch` your `options` (`:trim-chars` `:case-sensitive?`). 
All dispatch functions takes as argument `:linked-component-key` (name for the input) and `options`

```Clojure
(dispatch [:options list-name options])
```

For setting the `dictionary` you need to dispatch your `dictionary` (list of your autocomplete options).
This dispatch function takes as arguments `linked-component-key` (name for the input) and `dictionary`

```Clojure
(dispatch [:options list-name dictionary])
```

The last item you need to dispatch is your `input`.
This dispatch function takes as arguments `linked-component-key` (name for the input) and `input`

```Clojure
(dispatch [:options list-name input])
```
[example of the use](https://github.com/ScalaConsultants/re-complete/blob/master/demo/re_complete/example.cljs#L62)

```clojure
((dispatch [:options "vegetable" {:trim-chars "()",
                                  :sort-fn count}])
 (dispatch [:dictionary "vegetable" '("broccoli" "asparagus" "appricot" "cale")])
     (fn []
        [:ul
           [:li
            [:input {:type "text"
                     :value ""
                     :on-change (fn [event]
                                  (dispatch [:input list-name (.. event -target -value)]))}]]]
            [:div.autocompletion-list-part
          [re-complete/completions "vegetable]]))
```

When the change of input occurred (for example we will write `a` to input), in our app-state we will have

```clojure
{:autocomplete {:linked-components                                                                                                                                                                          
                {:vegetable {:text ""
                             :change-index 0
                             :current-word "a"
                             :completions ["appricot" "asparagus"]
                             :dictionary '("broccoli" "asparagus" "appricot" "cale")
                             :options {:trim-chars "()",
                                       :sort-fn count
                                       :case-sensitive? false}}}}}
```

The last function we need is `completions`
This function displays list of the items for autocompletition. After click on the item, the item is placed in the right position in text. 

If you want to use custom callback function, you can add it as additional optional argument to `completions` function.

`completions` takes as an argument `linked-component-key` - name of the input and optional argument `onclick-callback`


[example of the use](https://github.com/ScalaConsultants/re-complete/blob/master/demo/re_complete/example.cljs#L91)

```clojure
[:div.autocompletion-list-part
  [autocomplete/completions "vegetable"]]
```

# CSS styling

The `autocompletion-list` function renders `ul` with class `autocompletion-list` and `li` items with class `autocompletion-item`.

Enjoy the library!

# License

Copyright © 2016

Distributed under the Eclipse Public License.

