import {bindable, bindingMode, inject,BindingEngine} from 'aurelia-framework';

const concat = (xs,y) => xs.concat(y)
const flatMap = (f,xs) => xs.map(f).reduce(concat, [])

@inject(BindingEngine)
export class SkillsFilterCustomElement {
  @bindable({ defaultBindingMode: bindingMode.twoWay }) skills = [];
  selectedFilters = [];
  allSkills = [];
  tags = [];
  activeTags = [];
  constructor(bindingEngine) {
    this.subscription = bindingEngine.propertyObserver(this, 'skills')
      .subscribe((newValue, oldValue) => this.init(newValue));
  }
  init(skills) {
    this.allSkills = skills
    this.tags = flatMap(s => s.tags, this.allSkills).filter((value, index, self) => self.indexOf(value) === index)
  }
  attached() {
    if(this.skills) this.init(this.skills);
  }
  toggleTag(tag) {
    let index = this.activeTags.indexOf(tag)
    if(index === -1) {
      this.activeTags.push(tag)
    }else {
      this.activeTags.splice(index, -1)
    }
    this.refresh();
  }
  refresh() {
    this.skills = this.allSkills.filter(s => this.activeTags.length == 0 || this.activeTags.some(t => this.activeTags.indexOf(t) > -1));
  }
  dispose() {
    this.subscription.dispose();
  }
}
